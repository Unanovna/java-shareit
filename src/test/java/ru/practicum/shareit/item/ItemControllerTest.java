package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    private static final String USER_ID_IN_HEADER = "X-Sharer-User-Id";
    @MockBean
    ItemService itemService;
    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;
    Item item;
    ItemDto itemDto;

    @BeforeEach
    void beforeEach() {
        User user = User.builder().id(1L).name("user1").email("user1@mail.ru").build();
        item = Item.builder().id(1L).name("item1").description("itemDescription1").available(true)
                .owner(user).request(null).build();
        itemDto = ItemMapper.toItemDto(item);
    }

    @Test
    void createItemIsOk() throws Exception {
        when(itemService.add(anyLong(), any(ItemDto.class))).thenReturn(ItemMapper.toItemDto(item));
        mvc.perform(post("/items")
                        .header(USER_ID_IN_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value(item.getName()))
                .andExpect(jsonPath("$.description").value(item.getDescription()))
                .andExpect(jsonPath("$.available").value(item.getAvailable()))
                .andExpect(jsonPath("$.lastBooking").isEmpty())
                .andExpect(jsonPath("$.nextBooking").isEmpty())
                .andExpect(jsonPath("$.comments").isEmpty())
                .andExpect(jsonPath("$.requestId").isEmpty());
        verify(itemService).add(anyLong(), any());
    }

    @Test
    void updateItemIsOk() throws Exception {
        Map<String, String> map = Map.of(
                "name", "namUpdate",
                "description", "descriptionUpdate",
                "available", "false"
        );
        item.setName(map.get("name"));
        item.setDescription(map.get("description"));
        item.setAvailable(Boolean.valueOf(map.get("available")));
        when(itemService.update(anyLong(), anyLong(), anyMap())).thenReturn(ItemMapper.toItemDto(item));
        mvc.perform(patch("/items/{itemId}", 1)
                        .header(USER_ID_IN_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item.getId()))
                .andExpect(jsonPath("$.description").value(map.get("description")))
                .andExpect(jsonPath("$.available").value(map.get("available")))
                .andExpect(jsonPath("$.name").value(map.get("name")))
                .andExpect(jsonPath("$.requestId").isEmpty())
                .andExpect(jsonPath("$.lastBooking").isEmpty())
                .andExpect(jsonPath("$.nextBooking").isEmpty())
                .andExpect(jsonPath("$.comments").isEmpty());
        verify(itemService).update(anyLong(), anyLong(), anyMap());
    }

    @Test
    void updateItemWithIncorrectId() throws Exception {
        when(itemService.update(anyLong(), anyLong(), anyMap())).thenThrow(NotFoundException.class);
        mvc.perform(patch("/items/{itemId}", 1)
                        .header(USER_ID_IN_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(itemService).update(anyLong(), anyLong(), anyMap());
    }

    @Test
    void getItemByCorrectId() throws Exception {
        when(itemService.getItemDtoById(anyLong(), anyLong())).thenReturn(itemDto);
        mvc.perform(get("/items/{itemId}", 1L)
                        .header(USER_ID_IN_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()))
                .andExpect(jsonPath("$.requestId").isEmpty())
                .andExpect(jsonPath("$.lastBooking").isEmpty())
                .andExpect(jsonPath("$.nextBooking").isEmpty())
                .andExpect(jsonPath("$.comments").isEmpty());
        verify(itemService).getItemDtoById(anyLong(), anyLong());
    }

    @Test
    void getByIncorrectId() throws Exception {
        when(itemService.getItemDtoById(anyLong(), anyLong())).thenThrow(NotFoundException.class);
        mvc.perform(get("/items/{itemId}", 1L)
                        .header(USER_ID_IN_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(itemService).getItemDtoById(anyLong(), anyLong());
    }

    @Test
    void getAllUserItemsIsOk() throws Exception {
        when(itemService.getAllUserItems(anyLong(), anyInt(), anyInt())).thenReturn(List.of(ItemMapper.toItemDto(item)));
        mvc.perform(get("/items")
                        .header(USER_ID_IN_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*]").exists())
                .andExpect(jsonPath("$.[*]").isNotEmpty())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$.[0].id").value(item.getId()))
                .andExpect(jsonPath("$.[0].description").value(item.getDescription()))
                .andExpect(jsonPath("$.[0].available").value(item.getAvailable()))
                .andExpect(jsonPath("$.[0].name").value(item.getName()))
                .andExpect(jsonPath("$.[0].requestId").isEmpty())
                .andExpect(jsonPath("$.[0].lastBooking").isEmpty())
                .andExpect(jsonPath("$.[0].nextBooking").isEmpty())
                .andExpect(jsonPath("$.[0].comments").isEmpty());
        verify(itemService).getAllUserItems(anyLong(), anyInt(), anyInt());
    }

    @Test
    void getAllUserItemsWithoutItem() throws Exception {
        when(itemService.getAllUserItems(anyLong(), anyInt(), anyInt())).thenReturn(List.of());
        mvc.perform(get("/items")
                        .header(USER_ID_IN_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*]").isEmpty());
        verify(itemService).getAllUserItems(anyLong(), anyInt(), anyInt());
    }

    @Test
    void getAllByOwnerWithBadPage() throws Exception {
        mvc.perform(get("/items")
                        .header(USER_ID_IN_HEADER, 1L)
                        .param("from", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        verify(itemService, never()).getAllUserItems(anyLong(), anyInt(), anyInt());
    }

    @Test
    void getAllByOwnerWithBadSize() throws Exception {
        mvc.perform(get("/items")
                        .header(USER_ID_IN_HEADER, 1L)
                        .param("size", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        verify(itemService, never()).getAllUserItems(anyLong(), anyInt(), anyInt());
    }

    @Test
    void searchItemsWithOkRequest() throws Exception {
        when(itemService.searchItems(anyString(), anyInt(), anyInt())).thenReturn(List.of(itemDto));
        mvc.perform(get("/items/search")
                        .header(USER_ID_IN_HEADER, 1L)
                        .param("text", "caPch")
                        .param("from", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*]").exists())
                .andExpect(jsonPath("$.[*]").isNotEmpty())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$.[0].id").value(itemDto.getId()))
                .andExpect(jsonPath("$.[0].description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.[0].available").value(itemDto.getAvailable()))
                .andExpect(jsonPath("$.[0].name").value(itemDto.getName()))
                .andExpect(jsonPath("$.[0].requestId").isEmpty())
                .andExpect(jsonPath("$.[0].lastBooking").isEmpty())
                .andExpect(jsonPath("$.[0].nextBooking").isEmpty())
                .andExpect(jsonPath("$.[0].comments").isEmpty());
        verify(itemService).searchItems(anyString(), anyInt(), anyInt());
    }

    @Test
    void searchItemsWithBadPage() throws Exception {
        mvc.perform(get("/items/search")
                        .header(USER_ID_IN_HEADER, 1L)
                        .param("text", "item")
                        .param("from", "-1")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        verify(itemService, never()).searchItems(anyString(), anyInt(), anyInt());
    }

    @Test
    void searchItemsWithBadSize() throws Exception {
        mvc.perform(get("/items/search")
                        .header(USER_ID_IN_HEADER, 1L)
                        .param("text", "item")
                        .param("from", "0")
                        .param("size", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        verify(itemService, never()).searchItems(anyString(), anyInt(), anyInt());
    }

    @Test
    void addCommentIsOk() throws Exception {
        CommentDto commentDto = CommentDto.builder().id(1L).text("comment1").authorName("authorName1")
                .itemName("itemName1").created(LocalDateTime.now().plusMinutes(1)).build();
        when(itemService.addComment(anyLong(), anyLong(), any(CommentDto.class))).thenReturn(commentDto);
        mvc.perform(post("/items/{itemId}/comment", item.getId())
                        .header(USER_ID_IN_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(commentDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentDto.getId()))
                .andExpect(jsonPath("$.text").value(commentDto.getText()))
                .andExpect(jsonPath("$.authorName").value(commentDto.getAuthorName()))
                .andExpect(jsonPath("$.itemName").value(commentDto.getItemName()))
                .andExpect(jsonPath("$.created").isNotEmpty());
        verify(itemService).addComment(anyLong(), anyLong(), any(CommentDto.class));
    }
}
