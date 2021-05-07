package com.promineotech.jeep.controller;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import com.promineotech.jeep.entity.Jeep;
import com.promineotech.jeep.entity.JeepModel;

@RestController
@Slf4j
public class BasicJeepSalesController implements JeepSalesController {


	@Override
	public List<Jeep> fetchJeeps(JeepModel model, String trim) {
		log.debug ("model= {}, trim= {}", model, trim);
		return null;
	}

}
  