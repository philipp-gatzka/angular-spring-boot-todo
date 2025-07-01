package ch.gatzka.service;

import ch.gatzka.data.entity.Task;
import ch.gatzka.data.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public Page<Task> getTasks(String searchTerm, Boolean completed, Pageable pageable) {
        log.debug("Fetching tasks with searchTerm: {}, completed: {}, page: {}", searchTerm, completed, pageable);

        Page<Task> result;
        if (searchTerm != null && completed != null) {
            result = taskRepository.findByTitleContainingIgnoreCaseAndCompleted(searchTerm, completed, pageable);
        } else if (searchTerm != null) {
            result = taskRepository.findByTitleContainingIgnoreCase(searchTerm, pageable);
        } else if (completed != null) {
            result = taskRepository.findByCompleted(completed, pageable);
        } else {
            result = taskRepository.findAll(pageable);
        }

        log.info("Found {} tasks", result.getTotalElements());
        return result;
    }

    @Transactional(readOnly = true)
    public Task getTask(Long id) {
        log.debug("Fetching task with id: {}", id);
        return taskRepository.findById(id).orElseThrow(() -> {
            log.error("Task not found with id: {}", id);
            return new EntityNotFoundException("Task not found with id: " + id);
        });
    }

    public Task createTask(Task task) {
        log.debug("Creating new task with title: {}", task.getTitle());
        Task savedTask = taskRepository.save(task);
        log.info("Created new task with id: {}", savedTask.getId());
        return savedTask;
    }

    public Task updateTask(Long id, Task task) {
        log.debug("Updating task with id: {}", id);

        Task existingTask = getTask(id);
        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setCompleted(task.isCompleted());

        Task updatedTask = taskRepository.save(existingTask);
        log.info("Updated task with id: {}", updatedTask.getId());
        return updatedTask;
    }

    public void deleteTask(Long id) {
        log.debug("Deleting task with id: {}", id);
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            log.info("Deleted task with id: {}", id);
        } else {
            log.error("Task not found with id: {}", id);
            throw new EntityNotFoundException("Task not found with id: " + id);
        }
    }

    @Transactional(readOnly = true)
    public List<Task> getTasksByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Fetching tasks between {} and {}", startDate, endDate);
        List<Task> tasks = taskRepository.findByDateRange(startDate, endDate);
        log.info("Found {} tasks in date range", tasks.size());
        return tasks;
    }

    @Transactional(readOnly = true)
    public long getOpenTasksCount() {
        log.debug("Counting open tasks");
        long count = taskRepository.countByCompletedFalse();
        log.info("Found {} open tasks", count);
        return count;
    }

    @Transactional(readOnly = true)
    public List<Task> getRecentTasks(LocalDateTime since) {
        log.debug("Fetching recent tasks since {}", since);
        List<Task> tasks = taskRepository.findRecentTasks(since);
        log.info("Found {} recent tasks", tasks.size());
        return tasks;
    }

    @Transactional(readOnly = true)
    public List<Task> getRecentlyUpdatedTasks() {
        log.debug("Fetching recently updated tasks");
        List<Task> tasks = taskRepository.findTop10ByOrderByUpdatedAtDesc();
        log.info("Found {} recently updated tasks", tasks.size());
        return tasks;
    }

}
