package ru.practicum.shareit.booking.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Sort SORT_BY_START_BY_DESC = Sort.by(Sort.Direction.DESC, "start");

    List<Booking> findAllByBookerId(long bookerId);

    List<Booking> findAllByBookerIdAndStatus(long bookerId, BookingStatus status);

    List<Booking> findAllByBookerIdAndStartAfter(long bookerId, LocalDateTime start);

    List<Booking> findAllByBookerIdAndEndBefore(long bookerId, LocalDateTime end);

    @Query(value = "select b from Booking b where b.booker.id = ?1 and b.start < ?2 and b.end > ?2")
    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfter(long bookerId, LocalDateTime dateTime);

    @Query(value = "select b from Booking b join fetch b.item as i join fetch i.owner as o " +
            " where o.id = :ownerId ")
    List<Booking> findAllByOwnerId(@Param("ownerId") Long ownerId);

    @Query(value = "select b from Booking b join fetch b.item as i join fetch i.owner as o " +
            " where o.id = :ownerId and b.status = :status")
    List<Booking> findAllByOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") BookingStatus status);

    @Query(value = "select b from Booking b join fetch b.item as i join fetch i.owner as o " +
            " where o.id = :ownerId and b.start > :dateTime")
    List<Booking> findAllByOwnerIdAndStartAfter(@Param("ownerId") Long ownerId,
                                                @Param("dateTime") LocalDateTime dateTime);

    @Query(value = "select b from Booking b join fetch b.item as i join fetch i.owner as o " +
            " where o.id = :ownerId and b.end < :dateTime")
    List<Booking> findAllByOwnerIdAndEndBefore(@Param("ownerId") Long ownerId,
                                               @Param("dateTime") LocalDateTime dateTime);


    @Query(value = "select b from Booking b join fetch b.item as i join fetch i.owner as o " +
            " where o.id = :ownerId and b.start < :dateTime and b.end > :dateTime")
    List<Booking> findAllByOwnerIdAndStartBeforeAndEndAfter(@Param("ownerId") Long ownerId,
                                                            @Param("dateTime") LocalDateTime dateTime);

    List<Booking> findAllByItemIdAndStatus(long itemId, BookingStatus status);

    Optional<Booking> findFirstByItemIdAndBookerIdAndStatusAndEndBefore(long itemId, long bookerId,
                                                                        BookingStatus status, LocalDateTime end);
}