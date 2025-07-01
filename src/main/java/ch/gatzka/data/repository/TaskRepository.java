package ch.gatzka.data.repository;

import ch.gatzka.data.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    Page<Task> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Task> findByCompleted(boolean completed, Pageable pageable);

    Page<Task> findByTitleContainingIgnoreCaseAndCompleted(String title, boolean completed, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<Task> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    long countByCompletedFalse();

    @Query("SELECT t FROM Task t WHERE t.createdAt >= :since ORDER BY t.createdAt DESC")
    List<Task> findRecentTasks(@Param("since") LocalDateTime since);

    List<Task> findTop10ByOrderByUpdatedAtDesc();


}
