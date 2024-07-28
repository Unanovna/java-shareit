package org.example.booking.repository;

import org.example.booking.model.Booking;
import org.example.booking.model.BookingStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {
    Sort SORT_BY_START_BY_DESC = Sort.by(Sort.Direction.DESC, "start");

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