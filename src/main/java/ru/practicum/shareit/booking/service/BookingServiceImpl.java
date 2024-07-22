package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    @SuppressWarnings("unchecked")
    public Page<OutputBookingDto> getBookingsOfBooker(String stateText, Long bookerId, int from, int size) {
        getUserById(bookerId);
        State state = State.getState(stateText);
        Pageable pageable = PageRequest.of(size == 0 ? 0 : from / size, size, BookingRepository.SORT_BY_START_BY_DESC);
        Specification<Booking> spec = (root, query, cb) -> {
            Join<Object, Object> bookerJoin = (Join<Object, Object>) root.fetch("booker");
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(bookerJoin.get("id"), bookerId));
            predicates.addAll(getPredicates(root, cb, state));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return bookingRepository.findAll(spec, pageable).map(BookingMapper::toOutputBookingDto);
    }

    @Transactional
    @Override
    @SuppressWarnings("unchecked")
    public Page<OutputBookingDto> getBookingsOfOwner(String stateText, Long ownerId, int from, int size) {
        getUserById(ownerId);

        State state = State.getState(stateText);
        Pageable pageable = PageRequest.of(size == 0 ? 0 : from / size, size, BookingRepository.SORT_BY_START_BY_DESC);
        Specification<Booking> spec = (root, query, cb) -> {
            Join<Object, Object> itemJoin = (Join<Object, Object>) root.fetch("item");
            Join<Object, Object> ownerJoin = (Join<Object, Object>) itemJoin.fetch("owner");
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(ownerJoin.get("id"), ownerJoin));
            predicates.addAll(getPredicates(root, cb, state));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return bookingRepository.findAll(spec, pageable).map(BookingMapper::toOutputBookingDto);
    }

    private List<Predicate> getPredicates(Root<Booking> root, CriteriaBuilder cb, State state) {
        List<Predicate> predicates = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case WAITING:
                predicates.add(cb.equal(root.get("status"), BookingStatus.WAITING));
                break;
            case REJECTED:
                predicates.add(cb.equal(root.get("status"), BookingStatus.REJECTED));
                break;
            case PAST:
                predicates.add(cb.lessThan(root.get("end"), now));
                break;
            case FUTURE:
                predicates.add(cb.greaterThan(root.get("start"), now));
                break;
            case CURRENT:
                predicates.add(cb.lessThan(root.get("start"), now));
                predicates.add(cb.greaterThan(root.get("end"), now));
                break;
        }

        return predicates;
    }
}