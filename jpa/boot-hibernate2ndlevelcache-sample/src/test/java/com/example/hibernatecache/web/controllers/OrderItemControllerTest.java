package com.example.hibernatecache.web.controllers;

import static com.example.hibernatecache.utils.AppConstants.PROFILE_TEST;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.hibernatecache.entities.Customer;
import com.example.hibernatecache.entities.Order;
import com.example.hibernatecache.entities.OrderItem;
import com.example.hibernatecache.model.query.FindOrderItemsQuery;
import com.example.hibernatecache.model.request.OrderItemRequest;
import com.example.hibernatecache.model.response.OrderItemResponse;
import com.example.hibernatecache.model.response.PagedResult;
import com.example.hibernatecache.services.OrderItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = OrderItemController.class)
@ActiveProfiles(PROFILE_TEST)
class OrderItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderItemService orderItemService;

    @Autowired
    private ObjectMapper objectMapper;

    private List<OrderItem> orderItemList;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        Customer savedCustomer = new Customer()
                .setId(1L)
                .setFirstName("firstName 1")
                .setLastName("lastName 1")
                .setEmail("email1@junit.com")
                .setPhone("9876543211");
        savedOrder = new Order().setName("First Order").setPrice(BigDecimal.TEN).setCustomer(savedCustomer);
        orderItemList = new ArrayList<>();
        orderItemList.add(new OrderItem()
                .setId(1L)
                .setPrice(BigDecimal.TEN)
                .setQuantity(10)
                .setOrder(savedOrder));
        orderItemList.add(new OrderItem()
                .setId(2L)
                .setPrice(BigDecimal.TWO)
                .setQuantity(5)
                .setOrder(savedOrder));
        orderItemList.add(new OrderItem()
                .setId(3L)
                .setPrice(BigDecimal.ONE)
                .setQuantity(1)
                .setOrder(savedOrder));
    }

    @Test
    void shouldFetchAllOrderItems() throws Exception {

        Page<OrderItem> page = new PageImpl<>(orderItemList);
        PagedResult<OrderItemResponse> orderItemPagedResult = new PagedResult<>(page, getOrderItemResponseList());
        FindOrderItemsQuery findOrderItemsQuery = new FindOrderItemsQuery(0, 10, "id", "asc");
        given(orderItemService.findAllOrderItems(findOrderItemsQuery)).willReturn(orderItemPagedResult);

        this.mockMvc
                .perform(get("/api/order/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size()", is(orderItemList.size())))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.pageNumber", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.isLast", is(true)))
                .andExpect(jsonPath("$.hasNext", is(false)))
                .andExpect(jsonPath("$.hasPrevious", is(false)));
    }

    @Test
    void shouldFindOrderItemById() throws Exception {
        Long orderItemId = 1L;
        OrderItemResponse orderItem = new OrderItemResponse(orderItemId, "ITM3", BigDecimal.TEN, 10);
        given(orderItemService.findOrderItemById(orderItemId)).willReturn(Optional.of(orderItem));

        this.mockMvc
                .perform(get("/api/order/items/{id}", orderItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderItemId", is(orderItemId), Long.class))
                .andExpect(jsonPath("$.price", is(orderItem.price()), BigDecimal.class));
    }

    @Test
    void shouldReturn404WhenFetchingNonExistingOrderItem() throws Exception {
        Long orderItemId = 999L;
        given(orderItemService.findOrderItemById(orderItemId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(get("/api/order/items/{id}", orderItemId))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-hibernate2ndlevelcache-sample.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("OrderItem with Id '%d' not found".formatted(orderItemId)));
    }

    @Test
    void shouldUpdateOrderItem() throws Exception {
        Long orderItemId = 1L;
        OrderItemResponse orderItem = new OrderItemResponse(orderItemId, "ITM2", BigDecimal.TWO, 2);
        OrderItemRequest orderItemRequest = new OrderItemRequest(BigDecimal.TWO, 2, "IMT2");
        given(orderItemService.updateOrderItem(eq(orderItemId), any(OrderItemRequest.class)))
                .willReturn(orderItem);

        this.mockMvc
                .perform(put("/api/order/items/{id}", orderItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderItemRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderItemId", is(orderItemId), Long.class))
                .andExpect(jsonPath("$.price", is(orderItem.price()), BigDecimal.class));
    }

    @Test
    void shouldDeleteOrderItem() throws Exception {
        Long orderItemId = 1L;
        OrderItemResponse orderItem = new OrderItemResponse(orderItemId, "ITM1", BigDecimal.TEN, 10);
        given(orderItemService.findOrderItemById(orderItemId)).willReturn(Optional.of(orderItem));
        doNothing().when(orderItemService).deleteOrderItemById(orderItemId);

        this.mockMvc
                .perform(delete("/api/order/items/{id}", orderItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderItemId", is(orderItemId), Long.class))
                .andExpect(jsonPath("$.price", is(orderItem.price()), BigDecimal.class));
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingOrderItem() throws Exception {
        Long orderItemId = 999L;
        given(orderItemService.findOrderItemById(orderItemId)).willReturn(Optional.empty());

        this.mockMvc
                .perform(delete("/api/order/items/{id}", orderItemId))
                .andExpect(header().string("Content-Type", is(MediaType.APPLICATION_PROBLEM_JSON_VALUE)))
                .andExpect(jsonPath("$.type", is("http://api.boot-hibernate2ndlevelcache-sample.com/errors/not-found")))
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail").value("OrderItem with Id '%d' not found".formatted(orderItemId)));
    }

    List<OrderItemResponse> getOrderItemResponseList() {
        return orderItemList.stream()
                .map(orderItem -> new OrderItemResponse(
                        orderItem.getId(), orderItem.getItemCode(), orderItem.getPrice(), orderItem.getQuantity()))
                .toList();
    }
}
