package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.InputBookingDto;
import ru.practicum.shareit.booking.dto.OutputBookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.util.PageUtil;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.constant.HeaderConstant.USER_ID_IN_HEADER;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    BookingService bookingService;

    InputBookingDto inputBookingDto;
    OutputBookingDto outputBookingDto;
    Booking booking;
    User user;
    Item item;

    @BeforeEach
    void beforeEach() {
        LocalDateTime start = LocalDateTime.now().plusMinutes(1);
        LocalDateTime end = LocalDateTime.now().plusMinutes(30);
        user = User.builder().id(1L).name("user1").email("user1@mail.ru").build();
        item = Item.builder().id(1L).name("item1").description("itemDescription1").available(true)
                .owner(user).request(null).build();
        booking = Booking.builder().id(1L).item(item).booker(user).status(BookingStatus.WAITING)
                .start(start).end(end).build();
        outputBookingDto = BookingMapper.toOutputBookingDto(booking);
        inputBookingDto = new InputBookingDto(item.getId(), start, end);
    }

    @Test
    void createIsOk() throws Exception {
        when(bookingService.create(any(), anyLong())).thenReturn(outputBookingDto);
        mvc.perform(post("/bookings")
                        .header(USER_ID_IN_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(inputBookingDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking.getId()))
                .andExpect(jsonPath("$.start").isNotEmpty())
                .andExpect(jsonPath("$.end").isNotEmpty())
                .andExpect(jsonPath("$.status").value(booking.getStatus().toString()))
                .andExpect(jsonPath("$.booker.id").value(booking.getBooker().getId()))
                .andExpect(jsonPath("$.booker.name").value(booking.getBooker().getName()))
                .andExpect(jsonPath("$.item.id").value(booking.getItem().getId()))
                .andExpect(jsonPath("$.item.name").value(booking.getItem().getName()));
        verify(bookingService).create(any(), anyLong());
    }

    @Test
    void createWithTimeCrossing() throws Exception {
        inputBookingDto.setEnd(inputBookingDto.getStart().minusMinutes(30));
        System.out.println(inputBookingDto);
        mvc.perform(post("/bookings")
                        .header(USER_ID_IN_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(inputBookingDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(bookingService, never()).create(any(), anyLong());
    }

    @Test
    void approveBookingIsOk() throws Exception {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingService.approveBooking(anyLong(), anyLong(), anyBoolean())).thenReturn(BookingMapper
                .toOutputBookingDto(booking));
        mvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header(USER_ID_IN_HEADER, 1L)
                        .param("approved", String.valueOf(true))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking.getId()))
                .andExpect(jsonPath("$.start").isNotEmpty())
                .andExpect(jsonPath("$.end").isNotEmpty())
                .andExpect(jsonPath("$.status").value(booking.getStatus().toString()))
                .andExpect(jsonPath("$.booker.id").value(booking.getBooker().getId()));
        verify(bookingService).approveBooking(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    void getByIdIsOk() throws Exception {
        when(bookingService.getBookingDtoById(anyLong(), anyLong())).thenReturn(BookingMapper
                .toOutputBookingDto(booking));
        mvc.perform(get("/bookings/{bookingId}", 1L)
                        .header(USER_ID_IN_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking.getId()))
                .andExpect(jsonPath("$.start").isNotEmpty())
                .andExpect(jsonPath("$.end").isNotEmpty())
                .andExpect(jsonPath("$.status").value(booking.getStatus().toString()))
                .andExpect(jsonPath("$.booker.id").value(booking.getBooker().getId()))
                .andExpect(jsonPath("$.booker.name").value(booking.getBooker().getName()))
                .andExpect(jsonPath("$.item.id").value(booking.getItem().getId()))
                .andExpect(jsonPath("$.item.name").value(booking.getItem().getName()));
        verify(bookingService).getBookingDtoById(anyLong(), anyLong());
    }

    @Test
    void getByIdWithBadId() throws Exception {
        when(bookingService.getBookingDtoById(anyLong(), anyLong())).thenThrow(NotFoundException.class);
        mvc.perform(get("/bookings/{bookingId}", 1L)
                        .header(USER_ID_IN_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(bookingService).getBookingDtoById(anyLong(), anyLong());
    }

    @Test
    void getBookingsOfUserIsOk() throws Exception {
        when(bookingService.getBookingsOfBooker(any(), anyLong(), anyInt(), anyInt()))
                .thenReturn(
                        new PageImpl<>(List.of(BookingMapper.toOutputBookingDto(booking)),
                                PageUtil.getPageRequest(0, 10), 1)
                );
        mvc.perform(get("/bookings")
                        .header(USER_ID_IN_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[*]").exists())
                .andExpect(jsonPath("$.content.[*]").isNotEmpty())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content.[0].id").value(booking.getId()))
                .andExpect(jsonPath("$.content.[0].start").isNotEmpty())
                .andExpect(jsonPath("$.content.[0].end").isNotEmpty())
                .andExpect(jsonPath("$.content.[0].status").value(booking.getStatus().toString()))
                .andExpect(jsonPath("$.content.[0].booker.id").value(booking.getBooker().getId()))
                .andExpect(jsonPath("$.content.[0].booker.name").value(booking.getBooker().getName()))
                .andExpect(jsonPath("$.content.[0].item.id").value(booking.getItem().getId()))
                .andExpect(jsonPath("$.content.[0].item.name").value(booking.getItem().getName()));

        verify(bookingService).getBookingsOfBooker(any(), anyLong(), anyInt(), anyInt());
    }

    @Test
    void getBookingsOfBookerWithoutBooking() throws Exception {
        when(bookingService.getBookingsOfBooker(any(), anyLong(), anyInt(), anyInt())).thenReturn(
                new PageImpl<>(Collections.emptyList(), PageUtil.getPageRequest(0, 10), 0)
        );
        mvc.perform(get("/bookings")
                        .header(USER_ID_IN_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[*]").isEmpty());
        verify(bookingService).getBookingsOfBooker(any(), anyLong(), anyInt(), anyInt());
    }

    @Test
    void getBookingsOfBookerAndBadFrom() throws Exception {
        mvc.perform(get("/bookings")
                        .header(USER_ID_IN_HEADER, 1L)
                        .param("from", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        verify(bookingService, never()).getBookingsOfBooker(any(), anyLong(), anyInt(), anyInt());
    }

    @Test
    void getBookingsOfBookerAndBadSize() throws Exception {
        mvc.perform(get("/bookings")
                        .header(USER_ID_IN_HEADER, 1L)
                        .param("size", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        verify(bookingService, never()).getBookingsOfBooker(any(), anyLong(), anyInt(), anyInt());
    }

    @Test
    void getBookingsOfOwnerIsOk() throws Exception {
        when(bookingService.getBookingsOfOwner(any(), anyLong(), anyInt(), anyInt()))
                .thenReturn(
                        new PageImpl<>(List.of(BookingMapper.toOutputBookingDto(booking)),
                                PageUtil.getPageRequest(0, 10), 1)
                );
        mvc.perform(get("/bookings/owner")
                        .header(USER_ID_IN_HEADER, 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[*]").exists())
                .andExpect(jsonPath("$.content.[*]").isNotEmpty())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.content.[0].id").value(booking.getId()))
                .andExpect(jsonPath("$.content.[0].start").isNotEmpty())
                .andExpect(jsonPath("$.content.[0].end").isNotEmpty())
                .andExpect(jsonPath("$.content.[0].status").value(booking.getStatus().toString()))
                .andExpect(jsonPath("$.content.[0].booker.id").value(booking.getBooker().getId()))
                .andExpect(jsonPath("$.content.[0].booker.name").value(booking.getBooker().getName()))
                .andExpect(jsonPath("$.content.[0].item.id").value(booking.getItem().getId()))
                .andExpect(jsonPath("$.content.[0].item.name").value(booking.getItem().getName()));
        verify(bookingService).getBookingsOfOwner(any(), anyLong(), anyInt(), anyInt());
    }

    @Test
    void getBookingsOfOwnerWithLowCaseState() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .header(USER_ID_IN_HEADER, 1L)
                        .param("state", "waiting")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getBookingsOfOwnerAndBadFrom() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .header(USER_ID_IN_HEADER, 1L)
                        .param("from", "-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        verify(bookingService, never()).getBookingsOfOwner(any(), anyLong(), anyInt(), anyInt());
    }

    @Test
    void getBookingsOfOwnerAndBadSize() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .header(USER_ID_IN_HEADER, 1L)
                        .param("size", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        verify(bookingService, never()).getBookingsOfOwner(any(), anyLong(), anyInt(), anyInt());
    }
}
