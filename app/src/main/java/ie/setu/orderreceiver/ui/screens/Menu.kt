package ie.setu.orderreceiver.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ie.setu.orderreceiver.data.entities.MenuItem
import ie.setu.orderreceiver.ui.composables.CategoryPickerDialog
import ie.setu.orderreceiver.ui.viewmodels.MenuViewModel
import ie.setu.orderreceiver.R
import ie.setu.orderreceiver.utils.Categories

@Composable
fun Menu(
    navController: NavController,
    viewModel: MenuViewModel
) {
    Column {
        Menu(viewModel = viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Menu(viewModel: MenuViewModel) {
    val menuItems by viewModel.menu.collectAsState()
    var selectedCategoryFilter by remember { mutableStateOf<Categories?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            SwipeInstructionsPanel(
                menuLabel = if (selectedCategoryFilter == null) stringResource(id = R.string.menu) else stringResource(
                    id = selectedCategoryFilter!!.categoryNameResId
                )
            )
        }

        items(menuItems) { item ->
            MenuItemRow(menuItem = item, onDismiss = { menuItem, dismissValue ->
                when (dismissValue) {
                    DismissValue.DismissedToStart -> {
                        viewModel.deleteMenuItem(menuItem)
                    }

                    DismissValue.DismissedToEnd -> {
                        // Add to orders
                    }

                    else -> {}
                }
            })
        }

        item {
            CategoryPickerDialog(
                selectedCategory = selectedCategoryFilter,
                onCategorySelected = { category ->
                    Log.d("FILTER","User has selected the category ${category.name}")
                    selectedCategoryFilter = category
                    viewModel.getMenuItemsByCategory(category)
                    for (menuItem in menuItems) {
                       Log.d("FILTER","${menuItem.category.name} is now in the list is this right?")
                    }
                    showDialog = false
                },
                showDialog = showDialog,
                onConfirmDialog = {
                    showDialog = false
                    selectedCategoryFilter = null
                    viewModel.loadMenuItems()
                    Log.d("FILTER","Closing dialog and resetting vm menu items to all")
                },
                dialogButtonTextResId = R.string.reset_filter,
                onDismissDialog = {
                    showDialog = false
                }
            )
            Button(
                onClick = { showDialog = true }
            ) {
                Text(text = stringResource(id = R.string.filter_by_category))
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemRow(
    menuItem: MenuItem,
    onDismiss: (menuItem: MenuItem, dismissValue: DismissValue) -> Unit
) {
    val dismissState = rememberDismissState()

    SwipeToDismiss(
        state = dismissState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        background = {
            val color = when (dismissState.targetValue) {
                DismissValue.DismissedToStart -> Color.Red
                DismissValue.DismissedToEnd -> Color.Green
                else -> Color.Transparent
            }

            val icon = when (dismissState.targetValue) {
                DismissValue.DismissedToStart -> Icons.Default.DeleteForever
                DismissValue.DismissedToEnd -> Icons.Default.ShoppingCart
                else -> null
            }

            val iconAlignment = when (dismissState.targetValue) {
                DismissValue.DismissedToStart -> Alignment.CenterEnd
                else -> Alignment.CenterStart
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color),
                contentAlignment = iconAlignment
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        },
        dismissContent = {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context = LocalContext.current)
                            .data(menuItem.imageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = menuItem.name,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(100.dp)
                            .align(Alignment.CenterVertically),
                        contentScale = ContentScale.Crop
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = menuItem.name,
                            fontWeight = FontWeight.Bold
                        )
                        if (menuItem.description != null) {
                            Text(
                                text = menuItem.description,
                                fontStyle = FontStyle.Italic
                            )
                        }
                        Row {
                            Icon(
                                modifier = Modifier.size(16.dp),
                                imageVector = menuItem.category.categoryIcon,
                                contentDescription = stringResource(
                                    id = menuItem.category.categoryNameResId
                                )
                            )
                            Text(
                                stringResource(id = menuItem.category.categoryNameResId)
                            )
                        }
                        Text(
                            text = menuItem.price.toString(),
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    )
    if (dismissState.targetValue == DismissValue.DismissedToStart ||
        dismissState.targetValue == DismissValue.DismissedToEnd
    ) {
        onDismiss(menuItem, dismissState.currentValue)
    }
}

@Composable
fun SwipeInstructionsPanel(menuLabel: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RectangleShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Swipe right to add",
                    modifier = Modifier.size(24.dp)
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = menuLabel,
                fontWeight = FontWeight.Bold
            )
            Column {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Swipe left to delete",
                    modifier = Modifier.size(24.dp)
                )
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Swipe left to delete",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}