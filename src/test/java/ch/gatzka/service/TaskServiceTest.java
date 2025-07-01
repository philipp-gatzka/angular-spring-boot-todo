package ch.gatzka.service;

import ch.gatzka.data.entity.Task;
import ch.gatzka.data.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TaskService.class)
@ActiveProfiles("test")
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    private Task testTask;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        
        testTask = new Task();
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setCompleted(false);
        testTask = taskService.createTask(testTask);
    }

    @Test
    void createTask() {
        Task newTask = new Task();
        newTask.setTitle("New Task");
        newTask.setDescription("New Description");
        newTask.setCompleted(false);

        Task savedTask = taskService.createTask(newTask);

        assertNotNull(savedTask.getId());
        assertEquals("New Task", savedTask.getTitle());
        assertFalse(savedTask.isCompleted());
    }

    @Test
    void getTask() {
        Task foundTask = taskService.getTask(testTask.getId());
        
        assertEquals(testTask.getId(), foundTask.getId());
        assertEquals(testTask.getTitle(), foundTask.getTitle());
    }

    @Test
    void getTaskNotFound() {
        assertThrows(EntityNotFoundException.class, () -> 
            taskService.getTask(999L)
        );
    }

    @Test
    void getTasks() {
        // Create additional tasks
        createTestTask("Second Task", false);
        createTestTask("Third Task", true);

        Page<Task> allTasks = taskService.getTasks(null, null, PageRequest.of(0, 10));
        assertEquals(3, allTasks.getTotalElements());

        // Test search
        Page<Task> searchResults = taskService.getTasks("Second", null, PageRequest.of(0, 10));
        assertEquals(1, searchResults.getTotalElements());

        // Test completed filter
        Page<Task> completedTasks = taskService.getTasks(null, true, PageRequest.of(0, 10));
        assertEquals(1, completedTasks.getTotalElements());
    }

    @Test
    void updateTask() {
        Task updateData = new Task();
        updateData.setTitle("Updated Title");
        updateData.setDescription("Updated Description");
        updateData.setCompleted(true);

        Task updatedTask = taskService.updateTask(testTask.getId(), updateData);

        assertEquals("Updated Title", updatedTask.getTitle());
        assertEquals("Updated Description", updatedTask.getDescription());
        assertTrue(updatedTask.isCompleted());
    }

    @Test
    void deleteTask() {
        taskService.deleteTask(testTask.getId());
        assertThrows(EntityNotFoundException.class, () -> 
            taskService.getTask(testTask.getId())
        );
    }

    @Test
    void getTasksByDateRange() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        List<Task> tasks = taskService.getTasksByDateRange(startDate, endDate);
        assertFalse(tasks.isEmpty());
        assertEquals(1, tasks.size());
    }

    @Test
    void getOpenTasksCount() {
        createTestTask("Another Task", false);
        createTestTask("Completed Task", true);

        long openCount = taskService.getOpenTasksCount();
        assertEquals(2, openCount);
    }

    @Test
    void getRecentTasks() {
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        List<Task> recentTasks = taskService.getRecentTasks(since);
        assertFalse(recentTasks.isEmpty());
    }

    @Test
    void getRecentlyUpdatedTasks() {
        createTestTask("New Task 1", false);
        createTestTask("New Task 2", true);

        List<Task> recentlyUpdated = taskService.getRecentlyUpdatedTasks();
        assertFalse(recentlyUpdated.isEmpty());
        assertTrue(recentlyUpdated.size() <= 10);
    }

    private Task createTestTask(String title, boolean completed) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription("Description for " + title);
        task.setCompleted(completed);
        return taskService.createTask(task);
    }
}
