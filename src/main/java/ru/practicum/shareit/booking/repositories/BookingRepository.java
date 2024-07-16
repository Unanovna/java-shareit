package ru.practicum.shareit.booking.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Modifying
    @Query("UPDATE Booking b SET b.status = :status WHERE b.id = :bookingId")
    void updateStatus(@Param("status") BookingStatus status, @Param("bookingId") Long bookingId);

    List<Booking> findAllByBookerId(long bookerId, Sort sort);

    List<Booking> findAllByBookerIdAndStatus(long bookerId, BookingStatus status, Sort sort);

    List<Booking> findAllByBookerIdAndStartAfter(long bookerId, LocalDateTime start, Sort sort);

    List<Booking> findAllByBookerIdAndEndBefore(long bookerId, LocalDateTime end, Sort sort);

    @Query(value = "select b from Booking b where b.booker.id = ?1 and b.start < ?2 and b.end > ?2 order by b.start desc")
    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfter(long bookerId, LocalDateTime dateTime);

    @Query(value = "select b from Booking b join fetch b.item as i join fetch i.owner as o " +
            " where o.id = :ownerId order by b.start desc")
    List<Booking> findAllByOwnerId(@Param("ownerId") Long ownerId, Sort sort);

    @Query(value = "select b from Booking b join fetch b.item as i join fetch i.owner as o " +
            " where o.id = :ownerId and b.status = :status order by b.start desc")
    List<Booking> findAllByOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") BookingStatus status);

    @Query(value = "select b from Booking b join fetch b.item as i join fetch i.owner as o " +
            " where o.id = :ownerId and b.start > :dateTime  order by b.start desc")
    List<Booking> findAllByOwnerIdAndStartAfter(@Param("ownerId") Long ownerId,
                                                @Param("dateTime") LocalDateTime dateTime);

    @Query(value = "select b from Booking b join fetch b.item as i join fetch i.owner as o " +
            " where o.id = :ownerId and b.end < :dateTime  order by b.start desc")
    List<Booking> findAllByOwnerIdAndEndBefore(@Param("ownerId") Long ownerId,
                                               @Param("dateTime") LocalDateTime dateTime);

    @Query(value = "select b from Booking b join fetch b.item as i join fetch i.owner as o " +
            " where o.id = :ownerId and b.start < :dateTime and b.end > :dateTime order by b.start desc")
    List<Booking> findAllByOwnerIdAndStartBeforeAndEndAfter(@Param("ownerId") Long ownerId,
                                                            @Param("dateTime") LocalDateTime dateTime);

    List<Booking> findAllByItemIdAndStatus(long itemId, BookingStatus status);

    Optional<Booking> findFirstByItemIdAndBookerIdAndStatusAndEndBefore(long itemId, long bookerId,
                                                                        BookingStatus status, LocalDateTime end);
}