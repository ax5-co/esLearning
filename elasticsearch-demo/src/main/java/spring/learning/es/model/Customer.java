package spring.learning.es.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import spring.learning.es.helper.ElasticsearchIndices;

//Index of this model is defined in annotations style 
@Document(indexName = ElasticsearchIndices.CUSTOMER_INDEX)
@Setting(settingPath = "static/es-settings.json")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {

	@Id
	@Field(type = FieldType.Keyword)
	private String id;
	@Field(type = FieldType.Text)
	private String firstname;
	@Field(type = FieldType.Text)
	private String lastname;
	@Field(type = FieldType.Integer)
	private int age;
	
}
