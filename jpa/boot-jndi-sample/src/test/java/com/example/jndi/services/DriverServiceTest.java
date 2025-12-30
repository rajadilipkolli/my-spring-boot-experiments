package com.example.jndi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.willDoNothing;

import com.example.jndi.entities.Driver;
import com.example.jndi.mapper.DriverMapper;
import com.example.jndi.model.response.DriverResponse;
import com.example.jndi.repositories.DriverRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private DriverMapper driverMapper;

    @InjectMocks
    private DriverService driverService;

    @Test
    void findDriverById() {
        // given
        given(driverRepository.findById(1L)).willReturn(Optional.of(getDriver()));
        given(driverMapper.toResponse(any(Driver.class))).willReturn(getDriverResponse());
        // when
        Optional<DriverResponse> optionalDriver = driverService.findDriverById(1L);
        // then
        assertThat(optionalDriver).isPresent();
        DriverResponse driver = optionalDriver.get();
        assertThat(driver.id()).isOne();
        assertThat(driver.text()).isEqualTo("junitTest");
    }

    @Test
    void deleteDriverById() {
        // given
        willDoNothing().given(driverRepository).deleteById(1L);
        // when
        driverService.deleteDriverById(1L);
        // then
        verify(driverRepository, times(1)).deleteById(1L);
    }

    private Driver getDriver() {
        Driver driver = new Driver();
        driver.setId(1L);
        driver.setText("junitTest");
        return driver;
    }

    private DriverResponse getDriverResponse() {
        return new DriverResponse(1L, "junitTest");
    }
}
