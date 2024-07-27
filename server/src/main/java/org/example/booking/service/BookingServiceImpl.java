package org.example.booking.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.booking.InputBookingDto;
import org.example.booking.OutputBookingDto;
import org.example.booking.mapper.BookingMapper;
import org.example.booking.model.Booking;
import org.example.booking.model.BookingStatus;
import org.example.booking.model.State;
import org.example.booking.repository.BookingRepository;
import org.example.exception.ArgumentException;
import org.example.exception.InternalServerError;
import org.example.exception.NotFoundException;
import org.example.exception.ValidationException;
import org.example.item.model.Item;
import org.example.item.reposiory.ItemRepository;
import org.example.user.model.User;
import org.example.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.expression.AccessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static javax.swing.text.html.parser.DTDConstants.CURRENT;
import static org.example.booking.model.BookingStatus.REJECTED;
import static org.example.booking.model.BookingStatus.WAITING;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public Booking getBookingById(Long bookingId, Long userId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Booking with id: %d not found", bookingId)));
    }

    public void existsUser(Long userId) {
        if (!userRepository.existsUserById(userId)) {
            throw new InternalServerError(String.format("User with id %d not found", userId));
        }
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ArgumentException(String.format("User with id %d not found", userId)));
    }

    private Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Item with id %d not found", itemId)));
    }

    @Override
    @Transactional
    public OutputBookingDto create(InputBookingDto bookingDto, Long userId) {
        existsUser(userId);
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
                .status(WAITING)
                .build();
        return BookingMapper.toOutputBookingDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public OutputBookingDto approveBooking(Long bookingId, Long userId, Boolean isApprove) {
        existsUser(userId);
        Booking booking = getBookingById(bookingId, userId);
        Long itemOwnerId = getItemOwnerId(booking);
        Long bookerId = booking.getBooker().getId();
        if (!((userId.equals(itemOwnerId)) || (userId.equals(bookerId)))) {
            throw new ArgumentException(String.format("User id:%s cannot change Approve booking id:%s",
                    userId, booking.getId()));
        }
        if (!userId.equals(itemOwnerId)) {
            throw new AccessException(String.format("User id:%s cannot change Approve booking id:%s",
                    userId, booking.getId()));
        }
        if (booking.getStatus().equals(BookingStatus.APPROVED)) {
            throw new ArgumentException(String.format("Booking with id: %d already have status %s",
                    bookingId, BookingStatus.APPROVED));
        }
        BookingStatus bookingStatus = isApprove ? BookingStatus.APPROVED : REJECTED;
        booking.setStatus(bookingStatus);
        return BookingMapper.toOutputBookingDto(bookingRepository.save(booking));
    }

    private Long getItemOwnerId(Booking booking) {
        User booker = booking.getBooker();
        if (booker == null) {
            throw new InternalServerError("Booking with not installed!");
        }
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
        existsUser(userId);
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
    public Page<OutputBookingDto> getBookingsOfBooker(String stateText, Long bookerId, int from, int size) {
        existsUser(bookerId);
        State state = State.getState(stateText);
        Pageable pageable = PageRequest.of(size == 0 ? 0 : from / size, size, BookingRepository.SORT_BY_START_BY_DESC);
        List<Booking> bookings;
        switch (state) {
            case WAITING:
                bookings = bookingRepository.findAllByBookerIdAndStatus(bookerId, WAITING, pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByBookerIdAndStatus(bookerId, REJECTED, pageable);
                break;
            case PAST:
                bookings = bookingRepository.findAllByBookerIdAndEndBefore(bookerId, LocalDateTime.now(), pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByBookerIdAndStartAfter(bookerId, LocalDateTime.now(), pageable);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfter(bookerId, LocalDateTime.now(),
                        pageable);
                break;
            default:
                bookings = bookingRepository.findAllByBookerId(bookerId, pageable);
        }
        return BookingMapper.toOutputsBookingDtoList(bookings);
    }

    @Transactional
    @Override
    public Page<OutputBookingDto> getBookingsOfOwner(String stateText, Long ownerId, int from, int size) {
        existsUser(ownerId);
        State state = State.getState(stateText);
        Pageable pageable = PageRequest.of(size == 0 ? 0 : from / size, size, BookingRepository.SORT_BY_START_BY_DESC);
        List<Booking> bookings;
        switch (state) {
            case WAITING:
                bookings = bookingRepository.findAllByOwnerIdAndStatus(ownerId, WAITING, pageable);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByOwnerIdAndStatus(ownerId, REJECTED, pageable);
                break;
            case PAST:
                bookings = bookingRepository.findAllByOwnerIdAndEndBefore(ownerId, LocalDateTime.now(), pageable);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByOwnerIdAndStartAfter(ownerId, LocalDateTime.now(), pageable);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByOwnerIdAndStartBeforeAndEndAfter(ownerId, LocalDateTime.now(),
                        pageable);
                break;
            default:
                bookings = bookingRepository.findAllByOwnerId(ownerId, pageable);
        }
        return BookingMapper.toOutputsBookingDtoList(bookings);
    }
}