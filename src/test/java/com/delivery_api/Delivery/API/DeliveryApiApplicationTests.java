package com.delivery_api.Delivery.API;

import com.delivery_api.Delivery.API.Repository.AtefRepository;
import com.delivery_api.Delivery.API.Repository.RbfRepository;
import com.delivery_api.Delivery.API.Repository.WpefRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.PathVariable;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class DeliveryApiApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private RbfRepository rbfRepository;

	@Autowired
	private AtefRepository atefRepository;

	@Autowired
	private WpefRepository wpefRepository;

	@Test
	void testCalculateRbf() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/calculateRbf/Tallinn/Car"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(result -> {
					String content = result.getResponse().getContentAsString();
					double value;
					try {
						value = Double.parseDouble(content);
					} catch (NumberFormatException e) {
						throw new AssertionError("Response is not a valid double");
					}
					assertThat(value).isEqualTo(4.0);
				});
	}

	@Test
	void testCalculateRbfWithDateTime() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/calculateRbf/Tallinn/Bike/2024-03-08 12:00:00"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andDo(MockMvcResultHandlers.print())
				.andExpect(result -> {
					String content = result.getResponse().getContentAsString();
					double value;
					try {
						value = Double.parseDouble(content);
					} catch (NumberFormatException e) {
						throw new AssertionError("Response is not a valid double");
					}
					assertThat(value).isGreaterThan(0.0);
				});
	}

	@Test
	void testCalculateRbf_InvalidCity() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/calculateRbf/InvalidCity/Car"))
				.andExpect(MockMvcResultMatchers.status().isInternalServerError());
	}

	@Test
	void testCalculateRbf_InvalidVehicle() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/api/calculateRbf/Tallinn/InvalidVehicle"))
				.andExpect(MockMvcResultMatchers.status().isInternalServerError());
	}

	@Test
	void testChangeBaseFeeRules() throws Exception {
		String city = "Tartu";
		String vehicle = "Car";
		String newFee = "3";
		mockMvc.perform(MockMvcRequestBuilders.put("/api/changeBaseFeeRules/{forWhichCity}/{forWhichVehicle}/{fee}", city, vehicle, newFee)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string("Business rules updated"));

		double feeAfter = getFeeForCityAndVehicle(city, vehicle);
		assertThat(feeAfter).isEqualTo(Double.valueOf(newFee));
	}

	private double getFeeForCityAndVehicle(String city, String vehicle) {
		return rbfRepository.findByCityAndVehicle(city, vehicle).getFee();
	}

	@Test
	void testChangeExtraFeeRules() throws Exception {
		String table = "Atef";
		String oldValue = "<-10";
		String newValue = "<-11";
		String fee = "1.1";
		mockMvc.perform(MockMvcRequestBuilders.put("/api/changeExtraFeeRules/{table}/{oldValue}/{newValue}/{fee}", table, oldValue, newValue, fee)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string("Business rules updated"));

		double feeAfter = getFeeForValue(newValue);
		assertThat(feeAfter).isEqualTo(Double.valueOf(fee));
	}

	private double getFeeForValue(String value) {
		return atefRepository.findByBorders(value).getFee();
	}

	@Test
	void testAddExtraFeeRules() throws Exception {
		int rowCountBefore = getRowCount();

		mockMvc.perform(MockMvcRequestBuilders.post("/api/addExtraFeeRules/Wpef/sunny/0.2")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string("Business rules updated"));

		int rowCountAfter = getRowCount();
		assertThat(rowCountAfter).isEqualTo(rowCountBefore+1);
		assertThat(wpefRepository.findByContaining("sunny").getFee()).isEqualTo(0.2);
	}

	@Test
	void testDeleteExtraFeeRules() throws Exception {
		int rowCountBefore = getRowCount();

		mockMvc.perform(MockMvcRequestBuilders.delete("/api/deleteExtraFeeRules/Wpef/snow")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().string("Business rules updated"));

		int rowCountAfter = getRowCount();
		assertThat(rowCountAfter).isEqualTo(rowCountBefore-1);
	}

	private int getRowCount() {
		return wpefRepository.findAll().size();
	}
}
