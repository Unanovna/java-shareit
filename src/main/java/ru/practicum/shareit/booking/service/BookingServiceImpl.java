package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Sort;
import org.springframework.expression.AccessException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.InputBookingDto;
import ru.practicum.shareit.booking.dto.OutputBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.repositories.BookingRepository;
import ru.practicum.shareit.exception.InternalServerError;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id %d not found", userId)));
    }

    private Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Item with id %d not found", itemId)));
    }

    @SneakyThrows
    @Override
    @Transactional
    public OutputBookingDto create(InputBookingDto bookingDto, Long userId) {
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
        if (!item.isAvailable()) {
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
    public OutputBookingDto approveBooking(Long bookingId, Long userId, Boolean approve) {
        Booking booking = getBookingById(bookingId, userId);
        if (booking.getStatus().equals(BookingStatus.APPROVED)) {
            throw new ValidationException(String.format("Booking with id: %d already have status %s",
                    bookingId, BookingStatus.APPROVED));
        }
        checkAccessToBooking(booking, userId, false);
        BookingStatus bookingStatus = approve ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        booking.setStatus(bookingStatus);
        bookingRepository.updateStatus(bookingStatus, bookingId);
        return BookingMapper.toOutputBookingDto(bookingRepository.save(booking));
    }

    @Override
    public Booking getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Booking with id: %d not found", bookingId)));
        checkAccessToBooking(booking, userId, true);
        return booking;
    }

    @SneakyThrows
    private void checkAccessToBooking(Booking booking, Long userId, boolean accessForBooker) {
        User booker = booking.getBooker();
        if (booker == null) {
            throw new InternalServerError(String.format("For booking with id: %s Bouker is not installed!", booker.getId()));
        }
        Long bookerId = booker.getId();
        Item item = booking.getItem();
        if (item == null) {
            throw new InternalServerError(String.format("For booking with id: %s Item is not installed!", booker.getId()));
        }
        User owner = item.getOwner();
        if (owner == null) {
            throw new InternalServerError(String.format("For booking with id: %s Owner is not installed!", booker.getId()));
        }
        if (!owner.getId().equals(userId)) {
            throw new NotFoundException("wrong owner");
        }
        if (!accessForBooker && !bookerId.equals(userId)) {
            throw new AccessException(String.format("Access to User id:%s for booking id:%s is denied",
                    userId, booking.getId()));
        }
    }

    @Override
    public OutputBookingDto getBookingDtoById(Long bookingId, Long userId) {
        Booking booking = getBookingById(bookingId, userId);
        return BookingMapper.toOutputBookingDto(booking);
    }

    @Transactional
    @Override
    public List<OutputBookingDto> getBookingsOfBooker(State state, Long bookerId) {
        getUserById(bookerId);
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> bookings;
        switch (state) {
            case WAITING:
                bookings = bookingRepository.findAllByBookerIdAndStatus(bookerId, BookingStatus.WAITING, sort);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByBookerIdAndStatus(bookerId, BookingStatus.REJECTED, sort);
                break;
            case PAST:
                bookings = bookingRepository.findAllByBookerIdAndEndBefore(bookerId, LocalDateTime.now(), sort);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByBookerIdAndStartAfter(bookerId, LocalDateTime.now(), sort);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfter(bookerId, LocalDateTime.now());
                break;
            default:
                bookings = bookingRepository.findAllByBookerId(bookerId, sort);
        }
        return BookingMapper.toOutputsBookingDtoList(bookings);
    }

    @Transactional
    @Override
    public List<OutputBookingDto> getBookingsOfOwner(State state, Long ownerId) {
        getUserById(ownerId);
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> bookings;
        switch (state) {
            case WAITING:
                bookings = bookingRepository.findAllByOwnerIdAndStatus(ownerId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByOwnerIdAndStatus(ownerId, BookingStatus.REJECTED);
                break;
            case PAST:
                bookings = bookingRepository.findAllByOwnerIdAndEndBefore(ownerId, LocalDateTime.now());
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByOwnerIdAndStartAfter(ownerId, LocalDateTime.now());
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByOwnerIdAndStartBeforeAndEndAfter(ownerId, LocalDateTime.now());
                break;
            default:
                bookings = bookingRepository.findAllByOwnerId(ownerId, sort);
        }
        return BookingMapper.toOutputsBookingDtoList(bookings);
    }
}
