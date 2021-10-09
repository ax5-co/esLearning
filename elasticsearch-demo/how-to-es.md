1- create the models (e.g., Product)
2- create the es-settings.json 
3- create the mappings files, e.g., product.json. Give it the model's name
	to be easily retrieved later. 
4- create the indices, which are pretty much like repositories,
	but manually created, e.g., IndexService, and be careful with imports
	as elasticsearch has some class with a similar name. 
	4.1- remember that elasticsearch will save the index it creates. so consider
		if we want to re-create ALL the indices every time or not. The typical 
		situation is "not" but if we add some fields to a model for example,
		it would be correct to recreate its index. 
	4.2- so, get the es-settings json file (which basically has an empty "index"
		list), then for each model's name, get the json mappings file and pass
		the es-settings & model's json mappings to a CreateIndexRequest instance
		then pass that CreateIndexRequest instance to an autowired
		RestHighLevelClient client.indices().create(CreateIndexRequest)
	4.3- make sure you don't import CreateIndexRequest from admin's package, 
		import it from org.elasticsearch.client.indices.CreateIndexRequest. 
5- Now, to save a model instance in elasticsearch, use the .index() method of
	RestHighLevelClient client, by passing it an IndexRequest holding the new
	model instance and set the IndexRequest's index name, object id,
	source = the new object as string. 
	The index() method returns an IndexResponse with a RestStatus code.
6- To simply get indexed instance(s) by their id, initiate 
	RestHighLevelClient client.get(new GetRequest(indexName, id), RequestOptions)
	 which will put GET result in a GetResponse object whose "source" is the 
	 returned instances (records). 
	 THIS IS NOT THE SEARCH HITS. 
============================================================================

Match & MultiMatch

1- To create a match Query (or multiMatch), specify the fields you want to
	search in, the search term (query) and the operator (and/or) which means
	if (areej ahmad) is the term, then (or) results in returning every instance
	with either (areej) alone, (ahmad) alone, or both. 
	On the other hand, (and) results in returning the instances only if all 
	(areej ahmad) exists in the instance.
2- What we'll pass for the Query should have a List <String> fields, 
	String searchTerm, which in our example is passed through SearchRequestDTO
3- So, create a QueryBuilder instance (match/multimatch & configure it as in 1), 
	NOTE: use QueryBuilders.matchQuery(field, term) or .multiMatchQuery(term)
	.
	then pass that QuiryBuilder in postFilter() of a new SearchSourceBuilder, 
	then pass that SearchSourceBuilder as source() ...
	... to a new SearchRequest(indexName).
	This is how we create/build an ES SearchRequest  
4- Now let's use the SearchRequest to actually implement the search. 
	In the ModelService class, let's make a search(SearchRequestDto) and use the
	dto to create an ES SerachRequest request (point 3). 
	Next, invoke the RestHighLevelClient client.search(request, RequestOptions), 
	and get the returned SearchResponse response
	The found Json documents are in reponse.getHits().getHits()
	To get the Model instances from the json documents, use 
	 ObjectMapper.readValue(hit.getSourceAsString(), Model.class)
	and return that in a list or so. 
