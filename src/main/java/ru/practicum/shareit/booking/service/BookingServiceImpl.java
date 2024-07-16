package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.shareit.booking.dto.InputBookingDto;
import ru.practicum.shareit.booking.dto.OutputBookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.repositories.BookingRepository;
import ru.practicum.shareit.exception.AccessException;
import ru.practicum.shareit.exception.InternalServerError;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public Booking getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Booking with id: %d not found", bookingId)));
        return booking;
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id %d not found", userId)));
    }

    private Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Item with id %d not found", itemId)));
    }

    @Override
    @Transactional
    public OutputBookingDto create(InputBookingDto bookingDto, Long userId) {
        getUserById(userId);
        Long itemId = bookingDto.getItemId();
        Item item = getItemById(itemId);
        User owner = item.getOwner();
        if (owner == null) {
            throw new AccessException(String.format("Item with id = %d not have owner.", itemId));
        }
        if (owner.getId().equals(userId)) {
            throw new AccessException(String.format("Booker cannot be owner of item id: %d", userId));
        }
        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();
        if (end.isBefore(start) || end.equals(start)) {
            throw new ValidationException(String.format("Wrong booking time start = %s and end = %s", start, end));
        }
        if (!item.getAvailable()) {
            throw new ValidationException(String.format("Item with id: %d is not available!", userId));
        }
        Booking booking = Booking.builder()
                .start(start)
                .end(end)
                .item(item)
                .booker(getUserById(userId))
                .status(BookingStatus.WAITING)
                .build();
        return BookingMapper.toOutputBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public OutputBookingDto approveBooking(Long bookingId, Long userId, Boolean isApprove) {
        getUserById(userId);
        Booking booking = getBookingById(bookingId, userId);
        if (booking.getStatus().equals(BookingStatus.APPROVED)) {
            throw new ValidationException(String.format("Booking with id: %d already have status %s",
                    bookingId, BookingStatus.APPROVED));
        }
        if (!userId.equals(getItemOwnerId(booking))) {
            throw new AccessException(String.format("Access to User id:%s for booking id:%s is denied",
                    userId, booking.getId()));
        }
        BookingStatus bookingStatus = isApprove ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        booking.setStatus(bookingStatus);
        return BookingMapper.toOutputBookingDto(bookingRepository.save(booking));
    }

    private Long getItemOwnerId(Booking booking) {
        User booker = booking.getBooker();
        if (booker == null) {
            throw new InternalServerError(String.format("Booking with id: %s Bouker is not installed!", booker.getId()));
        }
        Long bookerId = booker.getId();
        Item item = booking.getItem();
        if (item == null) {
            throw new InternalServerError(String.format("Booking with id: %s Item is not installed!", booker.getId()));
        }
        User itemOwner = item.getOwner();
        if (itemOwner == null) {
            throw new InternalServerError(String.format("Booking with id: %s Owner is not installed!", booker.getId()));
        }
        return itemOwner.getId();
    }

    @Override
    public OutputBookingDto getBookingDtoById(Long bookingId, Long userId) {
        Booking booking = getBookingById(bookingId, userId);
        getUserById(userId);
        Long itemOwnerId = getItemOwnerId(booking);
        Long bookerId = booking.getBooker().getId();
        if (!((bookerId.equals(userId)) || (itemOwnerId.equals(userId)))) {
            throw new AccessException(String.format("Access to User id:%s for booking id:%s is denied",
                    userId, booking.getId()));
        }
        return BookingMapper.toOutputBookingDto(booking);
    }

    @Transactional
    @Override
    public List<OutputBookingDto> getBookingsOfBooker(String stateText, Long bookerId, int from, int size) {
        getUserById(bookerId);
        State state = State.getState(stateText);
        Pageable pageable = PageRequest.of(size == 0 ? 0 : from / size, size, BookingRepository.SORT_BY_START_BY_DESC);
        List<Booking> bookings;
        switch (state) {
            case WAITING:
                bookings = bookingRepository.findAllByBookerIdAndStatus(bookerId, BookingStatus.WAITING, (java.awt.print.Pageable) pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByBookerIdAndStatus(bookerId, BookingStatus.REJECTED, (java.awt.print.Pageable) pageable);
                break;
            case PAST:
                bookings = bookingRepository.findAllByBookerIdAndEndBefore(bookerId, LocalDateTime.now(), (java.awt.print.Pageable) pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByBookerIdAndStartAfter(bookerId, LocalDateTime.now(), (java.awt.print.Pageable) pageable);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfter(bookerId, LocalDateTime.now(),
                        (java.awt.print.Pageable) pageable);
                break;
            default:
                bookings = bookingRepository.findAllByBookerId(bookerId, (java.awt.print.Pageable) pageable);
        }
        return BookingMapper.toOutputsBookingDtoList(bookings);
    }

    @Transactional
    @Override
    public List<OutputBookingDto> getBookingsOfOwner(String stateText, Long ownerId, int from, int size) {
        getUserById(ownerId);
        State state = State.getState(stateText);
        Pageable pageable = PageRequest.of(size == 0 ? 0 : from / size, size, BookingRepository.SORT_BY_START_BY_DESC);
        List<Booking> bookings;
        switch (state) {
            case WAITING:
                bookings = bookingRepository.findAllByOwnerIdAndStatus(ownerId, BookingStatus.WAITING, (java.awt.print.Pageable) pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByOwnerIdAndStatus(ownerId, BookingStatus.REJECTED, (java.awt.print.Pageable) pageable);
                break;
            case PAST:
                bookings = bookingRepository.findAllByOwnerIdAndEndBefore(ownerId, LocalDateTime.now(), (java.awt.print.Pageable) pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByOwnerIdAndStartAfter(ownerId, LocalDateTime.now(), (java.awt.print.Pageable) pageable);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByOwnerIdAndStartBeforeAndEndAfter(ownerId, LocalDateTime.now(),
                        (java.awt.print.Pageable) pageable);
                break;
            default:
                bookings = bookingRepository.findAllByOwnerId(ownerId, (java.awt.print.Pageable) pageable);
        }
        return BookingMapper.toOutputsBookingDtoList(bookings);
    }
}