package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.util.PageUtil;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.constant.HeaderConstant.USER_ID_IN_HEADER;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private ItemRequestService itemRequestService;
    private final ItemRequestDto itemRequestDto = ItemRequestDto.builder()
            .id(1L)
            .description("Description11")
            .created(LocalDateTime.now().plusHours(1))
            .items(null)
            .build();
    private final ItemRequestDto inItemRequestDto = ItemRequestDto.builder().id(2L).description("Description2")
            .build();

    @Test
    void createIsOk() throws Exception {
        when(itemRequestService.add(any(), anyLong())).thenReturn(itemRequestDto);
        mvc.perform(post("/requests")
                        .header(USER_ID_IN_HEADER, 1L)
                        .content(mapper.writeValueAsString(inItemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemRequestDto.getId()))
                .andExpect(jsonPath("$.description").value(itemRequestDto.getDescription()))
                .andExpect(jsonPath("$.items").value(itemRequestDto.getItems()))
                .andExpect(jsonPath("$.created").value(itemRequestDto.getCreated().format(dateTimeFormatter)));
        verify(itemRequestService).add(any(), anyLong());
    }

    @Test
    void getUserRequestsIsOk() throws Exception {
        when(itemRequestService.getUserRequests(anyLong(), anyInt(), anyInt())).thenReturn(List.of(itemRequestDto));
        mvc.perform(get("/requests")
                        .header(USER_ID_IN_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(itemRequestDto.getId()))
                .andExpect(jsonPath("$[0].description").value(itemRequestDto.getDescription()))
                .andExpect(jsonPath("$[0].items").value(itemRequestDto.getItems()));
    }

    @Test
    void getUserRequestsWithEmptyResponse() throws Exception {
        when(itemRequestService.getUserRequests(anyLong(), anyInt(), anyInt())).thenReturn(List.of());
        mvc.perform(get("/requests")
                        .header(USER_ID_IN_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*]").isEmpty());
    }

    @Test
    void getUserRequestsWithNotFoundUser() throws Exception {
        when(itemRequestService.getUserRequests(anyLong(), anyInt(), anyInt())).thenThrow(NotFoundException.class);
        mvc.perform(get("/requests")
                        .header(USER_ID_IN_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserRequestsWithIncorrectFrom() throws Exception {
        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getUserRequestsWithIncorrectSize() throws Exception {
        mvc.perform(get("/requests")
                        .header(USER_ID_IN_HEADER, 1L)
                        .param("size", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        verify(itemRequestService, never()).getUserRequests(anyLong(), anyInt(), anyInt());
    }

    @Test
    void getOtherUsersRequestsIsOk() throws Exception {
        when(itemRequestService.getOtherUserRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(itemRequestDto), PageUtil.getPageRequest(0, 1), 1));
        mvc.perform(get("/requests/all")
                        .header(USER_ID_IN_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content.[0].id").value(itemRequestDto.getId()))
                .andExpect(jsonPath("$.content.[0].description").value(itemRequestDto.getDescription()))
                .andExpect(jsonPath("$.content.[0].items").value(itemRequestDto.getItems()));
    }

    @Test
    void getOtherUsersRequestsWithIncorrectFrom() throws Exception {
        mvc.perform(get("/requests/all")
                        .header(USER_ID_IN_HEADER, 1L)
                        .param("from", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getOtherUsersRequestsWithIncorrectSize() throws Exception {
        mvc.perform(get("/requests/all")
                        .header(USER_ID_IN_HEADER, 1L)
                        .param("size", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getRequestByIdIsOk() throws Exception {
        when(itemRequestService.getItemRequestById(anyLong(), anyLong())).thenReturn(itemRequestDto);
        mvc.perform(get("/requests/{requestId}", 1L)
                        .header(USER_ID_IN_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemRequestDto.getId()))
                .andExpect(jsonPath("$.description").value(itemRequestDto.getDescription()))
                .andExpect(jsonPath("$.created").isNotEmpty())
                .andExpect(jsonPath("$.items").isEmpty());
        verify(itemRequestService).getItemRequestById(anyLong(), anyLong());
    }

    @Test
    void findByIdWithNotFound() throws Exception {
        when(itemRequestService.getItemRequestById(anyLong(), anyLong())).thenThrow(NotFoundException.class);
        mvc.perform(get("/requests/{requestId}", 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
