package org.example.booking.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.example.booking.InputBookingDto;
import org.example.booking.OutputBookingDto;
import org.example.booking.State;
import org.example.booking.mapper.BookingMapper;
import org.example.booking.model.Booking;
import org.example.booking.model.BookingStatus;
import org.example.booking.repository.BookingRepository;
import org.example.exception.AccessException;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private User getUserByIdInternal(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new InternalServerError(String.format("User with id %d not found", userId)));
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
            throw new NotFoundException(String.format("Booker cannot be owner of item id: %d", userId));
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
        getUserByIdInternal(userId);
        Booking booking = getBookingById(bookingId, userId);
        if (booking.getStatus().equals(BookingStatus.APPROVED)) {
            throw new ValidationException(String.format("Booking with id: %d already have status %s",
                    bookingId, BookingStatus.APPROVED));
        }
        BookingStatus bookingStatus = isApprove ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        if (booking.getBooker().getId().equals(userId) && bookingStatus == BookingStatus.APPROVED) {
            throw new NotFoundException("Owner cant approve");
        }
        if (!userId.equals(getItemOwnerId(booking))) {
            throw new InternalServerError(String.format("Access to User id:%s for booking id:%s is denied",
                    userId, booking.getId()));
        }
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
            throw new NotFoundException(String.format("Access to User id:%s for booking id:%s is denied",
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
            Join<Object, Object> bookerJoin = root.join("booker");
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
            Join<Object, Object> itemJoin = root.join("item");
            Join<Object, Object> ownerJoin = itemJoin.join("owner");
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(ownerJoin.get("id"), ownerId));
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