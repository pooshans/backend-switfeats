package com.swifteats.restaurant.controller;

import com.swifteats.restaurant.dto.MenuItemDTO;
import com.swifteats.restaurant.service.MenuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuControllerTest {

    @Mock
    private MenuService menuService;

    @InjectMocks
    private MenuController menuController;

    private UUID testRestaurantId;
    private UUID testMenuItemId;
    private MenuItemDTO testMenuItem;
    private List<MenuItemDTO> testMenuItems;

    @BeforeEach
    void setUp() {
        testRestaurantId = UUID.randomUUID();
        testMenuItemId = UUID.randomUUID();

        // Create a test menu item
        testMenuItem = MenuItemDTO.builder()
                .id(testMenuItemId)
                .name("Spaghetti Carbonara")
                .description("Classic Italian pasta dish with eggs, cheese, pancetta, and pepper")
                .price(new BigDecimal("12.99"))
                .category("Pasta")
                .imageUrl("http://example.com/carbonara.jpg")
                .isAvailable(true)
                .preparationTimeMinutes(20)
                .restaurantId(testRestaurantId)
                .build();

        // Create a list of test menu items
        testMenuItems = Arrays.asList(testMenuItem);
    }

    @Test
    void getRestaurantMenu_shouldReturnRestaurantMenu() {
        // Arrange
        when(menuService.getMenuByRestaurantId(testRestaurantId)).thenReturn(testMenuItems);

        // Act
        ResponseEntity<List<MenuItemDTO>> response = menuController.getRestaurantMenu(testRestaurantId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<MenuItemDTO> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(1, responseBody.size());
        assertEquals(testMenuItem.getName(), responseBody.get(0).getName());
        assertEquals(testRestaurantId, responseBody.get(0).getRestaurantId());
        verify(menuService, times(1)).getMenuByRestaurantId(testRestaurantId);
    }

    @Test
    void getAvailableMenuItems_shouldReturnAvailableItems() {
        // Arrange
        when(menuService.getAvailableMenuItems(testRestaurantId)).thenReturn(testMenuItems);

        // Act
        ResponseEntity<List<MenuItemDTO>> response = menuController.getAvailableMenuItems(testRestaurantId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<MenuItemDTO> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(1, responseBody.size());
        assertTrue(responseBody.get(0).isAvailable());
        verify(menuService, times(1)).getAvailableMenuItems(testRestaurantId);
    }

    @Test
    void getMenuItemById_whenItemExists_shouldReturnMenuItem() {
        // Arrange
        when(menuService.getMenuItemById(testMenuItemId)).thenReturn(Optional.of(testMenuItem));

        // Act
        ResponseEntity<MenuItemDTO> response = menuController.getMenuItemById(testMenuItemId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        MenuItemDTO responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(testMenuItemId, responseBody.getId());
        assertEquals(testMenuItem.getName(), responseBody.getName());
        verify(menuService, times(1)).getMenuItemById(testMenuItemId);
    }

    @Test
    void getMenuItemById_whenItemDoesNotExist_shouldReturnNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(menuService.getMenuItemById(nonExistentId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<MenuItemDTO> response = menuController.getMenuItemById(nonExistentId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(menuService, times(1)).getMenuItemById(nonExistentId);
    }

    @Test
    void createMenuItem_whenSuccessful_shouldCreateAndReturnMenuItem() {
        // Arrange
        MenuItemDTO newItemRequest = MenuItemDTO.builder()
                .name("New Item")
                .description("New Description")
                .price(new BigDecimal("15.99"))
                .category("Appetizer")
                .build();

        when(menuService.createMenuItem(any(MenuItemDTO.class))).thenReturn(Optional.of(testMenuItem));

        // Act
        ResponseEntity<MenuItemDTO> response = menuController.createMenuItem(testRestaurantId, newItemRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        MenuItemDTO responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(testMenuItem.getId(), responseBody.getId());
        assertEquals(testMenuItem.getName(), responseBody.getName());

        // Verify that restaurantId was set on the DTO
        verify(menuService, times(1)).createMenuItem(
                argThat(dto -> dto.getRestaurantId() != null && dto.getRestaurantId().equals(testRestaurantId)));
    }

    @Test
    void createMenuItem_whenNotSuccessful_shouldReturnBadRequest() {
        // Arrange
        MenuItemDTO newItemRequest = MenuItemDTO.builder()
                .name("Invalid Item")
                .build();

        when(menuService.createMenuItem(any(MenuItemDTO.class))).thenReturn(Optional.empty());

        // Act
        ResponseEntity<MenuItemDTO> response = menuController.createMenuItem(testRestaurantId, newItemRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(menuService, times(1)).createMenuItem(any(MenuItemDTO.class));
    }

    @Test
    void updateMenuItem_whenItemExists_shouldUpdateAndReturnItem() {
        // Arrange
        MenuItemDTO updateRequest = MenuItemDTO.builder()
                .name("Updated Item")
                .description("Updated Description")
                .price(new BigDecimal("17.99"))
                .build();

        when(menuService.updateMenuItem(eq(testMenuItemId), any(MenuItemDTO.class)))
                .thenReturn(Optional.of(testMenuItem));

        // Act
        ResponseEntity<MenuItemDTO> response = menuController.updateMenuItem(testMenuItemId, updateRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        MenuItemDTO responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(testMenuItemId, responseBody.getId());
        assertEquals(testMenuItem.getName(), responseBody.getName());
        verify(menuService, times(1)).updateMenuItem(eq(testMenuItemId), any(MenuItemDTO.class));
    }

    @Test
    void updateMenuItem_whenItemDoesNotExist_shouldReturnNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        MenuItemDTO updateRequest = MenuItemDTO.builder()
                .name("Updated Item")
                .build();

        when(menuService.updateMenuItem(eq(nonExistentId), any(MenuItemDTO.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<MenuItemDTO> response = menuController.updateMenuItem(nonExistentId, updateRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(menuService, times(1)).updateMenuItem(eq(nonExistentId), any(MenuItemDTO.class));
    }

    @Test
    void deleteMenuItem_whenItemExists_shouldReturnNoContent() {
        // Arrange
        when(menuService.deleteMenuItem(testMenuItemId)).thenReturn(true);

        // Act
        ResponseEntity<Void> response = menuController.deleteMenuItem(testMenuItemId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(menuService, times(1)).deleteMenuItem(testMenuItemId);
    }

    @Test
    void deleteMenuItem_whenItemDoesNotExist_shouldReturnNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(menuService.deleteMenuItem(nonExistentId)).thenReturn(false);

        // Act
        ResponseEntity<Void> response = menuController.deleteMenuItem(nonExistentId);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(menuService, times(1)).deleteMenuItem(nonExistentId);
    }
}
