package com.swifteats.restaurant.controller;

import com.swifteats.restaurant.dto.MenuItemDTO;
import com.swifteats.restaurant.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/restaurants/{restaurantId}/menu")
    public ResponseEntity<List<MenuItemDTO>> getRestaurantMenu(@PathVariable UUID restaurantId) {
        log.info("REST request to get menu for restaurant ID: {}", restaurantId);
        return ResponseEntity.ok(menuService.getMenuByRestaurantId(restaurantId));
    }

    @GetMapping("/restaurants/{restaurantId}/menu/available")
    public ResponseEntity<List<MenuItemDTO>> getAvailableMenuItems(@PathVariable UUID restaurantId) {
        log.info("REST request to get available menu items for restaurant ID: {}", restaurantId);
        return ResponseEntity.ok(menuService.getAvailableMenuItems(restaurantId));
    }

    @GetMapping("/menus/{id}")
    public ResponseEntity<MenuItemDTO> getMenuItemById(@PathVariable UUID id) {
        log.info("REST request to get menu item with ID: {}", id);
        return menuService.getMenuItemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/restaurants/{restaurantId}/menu")
    public ResponseEntity<MenuItemDTO> createMenuItem(
            @PathVariable UUID restaurantId,
            @RequestBody MenuItemDTO menuItemDTO) {
        log.info("REST request to create menu item for restaurant ID: {}", restaurantId);

        menuItemDTO.setRestaurantId(restaurantId);
        return menuService.createMenuItem(menuItemDTO)
                .map(createdItem -> new ResponseEntity<>(createdItem, HttpStatus.CREATED))
                .orElse(ResponseEntity.badRequest().build());
    }

    @PutMapping("/menus/{id}")
    public ResponseEntity<MenuItemDTO> updateMenuItem(
            @PathVariable UUID id,
            @RequestBody MenuItemDTO menuItemDTO) {
        log.info("REST request to update menu item with ID: {}", id);

        return menuService.updateMenuItem(id, menuItemDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/menus/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable UUID id) {
        log.info("REST request to delete menu item with ID: {}", id);

        if (menuService.deleteMenuItem(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
