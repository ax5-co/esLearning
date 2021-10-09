package spring.learning.es.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

//Index of this model is defined in .json style 
@Data
public class Product {

	private String id;
	private String title;
	private String desc;
	@JsonFormat(pattern = "dd-MM-yyyy")
	private Date createdAt;
	private double price;
	
}
