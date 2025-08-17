package com.swifteats.restaurant.service;

import com.swifteats.restaurant.domain.MenuItem;
import com.swifteats.restaurant.domain.Restaurant;
import com.swifteats.restaurant.dto.MenuItemDTO;
import com.swifteats.restaurant.repository.MenuItemRepository;
import com.swifteats.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    @Cacheable(value = "menus", key = "#restaurantId")
    public List<MenuItemDTO> getMenuByRestaurantId(UUID restaurantId) {
        log.info("Fetching menu for restaurant ID: {}", restaurantId);
        return menuItemRepository.findByRestaurantId(restaurantId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "menus", key = "'available_' + #restaurantId")
    public List<MenuItemDTO> getAvailableMenuItems(UUID restaurantId) {
        log.info("Fetching available menu items for restaurant ID: {}", restaurantId);
        return menuItemRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<MenuItemDTO> getMenuItemById(UUID id) {
        log.info("Fetching menu item with ID: {}", id);
        return menuItemRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional
    @CacheEvict(value = "menus", key = "#menuItemDTO.restaurantId")
    public Optional<MenuItemDTO> createMenuItem(MenuItemDTO menuItemDTO) {
        log.info("Creating new menu item for restaurant ID: {}", menuItemDTO.getRestaurantId());

        return restaurantRepository.findById(menuItemDTO.getRestaurantId())
                .map(restaurant -> {
                    MenuItem menuItem = convertToEntity(menuItemDTO);
                    menuItem.setRestaurant(restaurant);
                    return convertToDTO(menuItemRepository.save(menuItem));
                });
    }

    @Transactional
    @CacheEvict(value = "menus", allEntries = true)
    public Optional<MenuItemDTO> updateMenuItem(UUID id, MenuItemDTO menuItemDTO) {
        log.info("Updating menu item with ID: {}", id);

        return menuItemRepository.findById(id)
                .map(existingItem -> {
                    updateEntityFromDTO(existingItem, menuItemDTO);
                    return convertToDTO(menuItemRepository.save(existingItem));
                });
    }

    @Transactional
    @CacheEvict(value = "menus", allEntries = true)
    public boolean deleteMenuItem(UUID id) {
        log.info("Deleting menu item with ID: {}", id);

        if (menuItemRepository.existsById(id)) {
            menuItemRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Helper methods for DTO conversion
    private MenuItemDTO convertToDTO(MenuItem menuItem) {
        return MenuItemDTO.builder()
                .id(menuItem.getId())
                .name(menuItem.getName())
                .description(menuItem.getDescription())
                .price(menuItem.getPrice())
                .category(menuItem.getCategory())
                .imageUrl(menuItem.getImageUrl())
                .isAvailable(menuItem.isAvailable())
                .preparationTimeMinutes(menuItem.getPreparationTimeMinutes())
                .restaurantId(menuItem.getRestaurant().getId())
                .build();
    }

    private MenuItem convertToEntity(MenuItemDTO dto) {
        return MenuItem.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .category(dto.getCategory())
                .imageUrl(dto.getImageUrl())
                .isAvailable(dto.isAvailable())
                .preparationTimeMinutes(dto.getPreparationTimeMinutes())
                .build();
    }

    private void updateEntityFromDTO(MenuItem entity, MenuItemDTO dto) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setCategory(dto.getCategory());
        entity.setImageUrl(dto.getImageUrl());
        entity.setAvailable(dto.isAvailable());
        entity.setPreparationTimeMinutes(dto.getPreparationTimeMinutes());
    }
}
