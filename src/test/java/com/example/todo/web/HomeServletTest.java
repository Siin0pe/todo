package com.example.todo.web;

import com.example.todo.model.Category;
import com.example.todo.service.CategoryService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeServletTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private RequestDispatcher dispatcher;
    @Mock
    private CategoryService categoryService;

    private HomeServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new TestHomeServlet(categoryService);
    }

    @Test
    void doGetLoadsCategoriesAndForwards() throws Exception {
        List<Category> categories = Collections.singletonList(new Category());
        when(categoryService.listCategories()).thenReturn(categories);
        when(request.getRequestDispatcher("/WebContent/Home.jsp")).thenReturn(dispatcher);
        when(request.getParameter("success")).thenReturn(" ok ");
        when(request.getParameter("error")).thenReturn(" ");

        servlet.doGet(request, response);

        verify(request).setAttribute("categories", categories);
        verify(request).setAttribute("success", "ok");
        verify(request).setAttribute("error", null);
        verify(dispatcher).forward(request, response);
    }

    private static class TestHomeServlet extends HomeServlet {
        private final CategoryService categoryService;

        private TestHomeServlet(CategoryService categoryService) {
            this.categoryService = categoryService;
        }

        @Override
        protected CategoryService categoryService() {
            return categoryService;
        }
    }
}
