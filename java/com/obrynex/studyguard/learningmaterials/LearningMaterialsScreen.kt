package com.obrynex.studyguard.learningmaterials

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.obrynex.studyguard.ui.adaptive.contentHorizontalPadding
import com.obrynex.studyguard.ui.theme.*

@Composable
fun LearningMaterialsScreen(
    vm: LearningMaterialsViewModel,
    windowSizeClass: WindowSizeClass? = null,
    onStudyModeClick: (LearningMaterial) -> Unit = {}
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val filteredMaterials = remember(state.materials, state.selectedCategory, state.searchQuery) {
        vm.getFilteredMaterials()
    }
    val hPad = windowSizeClass?.contentHorizontalPadding ?: 16.dp
    val isTwoPaneCapable = windowSizeClass?.widthSizeClass != WindowWidthSizeClass.Compact

    Scaffold(
        containerColor = BgDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = vm::showAddDialog,
                containerColor = AccentGreen,
                contentColor = BgDark,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, "Add Material")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgDark)
                .padding(padding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = hPad, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Learning Materials",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${state.materials.size} materials",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
                Icon(
                    Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = AccentGreen,
                    modifier = Modifier.size(32.dp)
                )
            }

            HorizontalDivider(color = Divider, thickness = 0.5.dp)

            // Search bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = vm::onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = hPad, vertical = 12.dp),
                placeholder = { Text("Search materials...", color = TextMuted.copy(alpha = 0.5f)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, null, tint = TextMuted)
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { vm.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Close, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGreen,
                    unfocusedBorderColor = Divider,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = Surface2,
                    unfocusedContainerColor = Surface2
                )
            )

            // Category filter chips
            if (state.categories.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = hPad),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedCategory == null,
                            onClick = { vm.onCategorySelected(null) },
                            label = { Text("All", fontSize = 12.sp) },
                            colors = filterChipColors(state.selectedCategory == null)
                        )
                    }
                    items(state.categories) { category ->
                        FilterChip(
                            selected = state.selectedCategory == category,
                            onClick = { vm.onCategorySelected(category) },
                            label = { Text(category, fontSize = 12.sp) },
                            colors = filterChipColors(state.selectedCategory == category)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            // Materials list
            if (filteredMaterials.isEmpty()) {
                EmptyMaterialsState(state.searchQuery.isNotEmpty() || state.selectedCategory != null)
            } else if (isTwoPaneCapable) {
                // ── Tablet/foldable: 2-column grid ───────────────────────
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = hPad, end = hPad, top = 8.dp, bottom = 88.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    gridItems(filteredMaterials, key = { it.id }) { material ->
                        MaterialCard(
                            material = material,
                            onStudy = {
                                vm.markAccessed(material)
                                onStudyModeClick(material)
                            },
                            onEdit = { vm.showEditDialog(material) },
                            onDelete = { vm.showDeleteDialog(material) }
                        )
                    }
                }
            } else {
                // ── Phone: single column ─────────────────────────────────
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = hPad, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredMaterials, key = { it.id }) { material ->
                        MaterialCard(
                            material = material,
                            onStudy = {
                                vm.markAccessed(material)
                                onStudyModeClick(material)
                            },
                            onEdit = { vm.showEditDialog(material) },
                            onDelete = { vm.showDeleteDialog(material) }
                        )
                    }
                    // Bottom spacer so last card isn't hidden behind the FAB
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }

    // Add dialog
    if (state.showAddDialog) {
        MaterialDialog(
            title = "Add Learning Material",
            titleValue = state.newTitle,
            contentValue = state.newContent,
            categoryValue = state.newCategory,
            onTitleChange = vm::onNewTitleChanged,
            onContentChange = vm::onNewContentChanged,
            onCategoryChange = vm::onNewCategoryChanged,
            onConfirm = vm::addMaterial,
            onDismiss = vm::dismissAddDialog,
            confirmLabel = "Add"
        )
    }

    // Edit dialog
    if (state.showEditDialog) {
        MaterialDialog(
            title = "Edit Material",
            titleValue = state.newTitle,
            contentValue = state.newContent,
            categoryValue = state.newCategory,
            onTitleChange = vm::onNewTitleChanged,
            onContentChange = vm::onNewContentChanged,
            onCategoryChange = vm::onNewCategoryChanged,
            onConfirm = vm::updateMaterial,
            onDismiss = vm::dismissEditDialog,
            confirmLabel = "Save"
        )
    }

    // Delete confirmation dialog
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = vm::dismissDeleteDialog,
            title = { Text("Delete Material?", color = TextPrimary) },
            text = {
                Text(
                    "\"${state.selectedMaterial?.title}\" will be permanently deleted.",
                    color = TextMuted
                )
            },
            confirmButton = {
                TextButton(onClick = vm::confirmDelete) {
                    Text("Delete", color = Color(0xFFFF5252))
                }
            },
            dismissButton = {
                TextButton(onClick = vm::dismissDeleteDialog) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = Surface2
        )
    }

    // Error snackbar
    if (state.error != null) {
        LaunchedEffect(state.error) {
            // Auto-clear after 3 seconds
            kotlinx.coroutines.delay(3000)
            vm.clearError()
        }
    }
}

@Composable
private fun MaterialCard(
    material: LearningMaterial,
    onStudy: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface2)
            .padding(16.dp)
    ) {
        // Top row: icon + title + actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AccentGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Book,
                        null,
                        tint = AccentGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        material.title,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (material.category.isNotBlank()) {
                        Text(
                            material.category,
                            color = AccentGreen.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
            Row {
                IconButton(onClick = onStudy, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Outlined.Book,
                        "Study",
                        tint = AccentBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        "Edit",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        "Delete",
                        tint = Color(0xFFFF5252).copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Content preview
        if (material.content.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            SelectionContainer {
                Text(
                    material.content,
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EmptyMaterialsState(isFiltered: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.MenuBook,
            contentDescription = null,
            tint = Surface3,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            if (isFiltered) "No matching materials" else "No learning materials yet",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(4.dp))
        Text(
            if (isFiltered) "Try a different search or filter" else "Tap + to add your first material",
            color = TextMuted,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun MaterialDialog(
    title: String,
    titleValue: String,
    contentValue: String,
    categoryValue: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmLabel: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = titleValue,
                    onValueChange = onTitleChange,
                    label = { Text("Title *", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = Divider,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = categoryValue,
                    onValueChange = onCategoryChange,
                    label = { Text("Category (optional)", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = Divider,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = contentValue,
                    onValueChange = onContentChange,
                    label = { Text("Content", color = TextMuted) },
                    modifier = Modifier.heightIn(min = 100.dp, max = 200.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = Divider,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(confirmLabel, color = BgDark, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = Surface2
    )
}

@Composable
private fun filterChipColors(isSelected: Boolean): SelectableChipColors =
    FilterChipDefaults.filterChipColors(
        selectedContainerColor = AccentGreen.copy(alpha = 0.2f),
        selectedLabelColor = AccentGreen,
        containerColor = Surface2,
        labelColor = if (isSelected) AccentGreen else TextMuted,
        selectedLeadingIconColor = AccentGreen,
        iconColor = TextMuted
    )
