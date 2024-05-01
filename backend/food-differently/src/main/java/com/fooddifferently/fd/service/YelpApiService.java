package com.fooddifferently.fd.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Service class for integrating with the Yelp API to retrieve restaurant information and reviews.
 */
@Service
public class YelpApiService {

    @Value("${yelp.api.url}")
    private String apiUrl;

    @Value("${yelp.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    /**
     * Constructor for the YelpApiService class.
     *
     * @param restTemplate The RestTemplate instance used for making HTTP requests.
     */
    public YelpApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Search for businesses on Yelp based on a term and location.
     *
     * @param term     The search term (e.g., restaurant name).
     * @param location The location for the search (e.g., city, zip code).
     * @return A list of businesses matching the search term and location.
     */
    public List<Map<String, Object>> searchBusinesses(String term, String location) {
        String url = apiUrl + "/businesses/search?term=" + term + "&location=" + location;
        return callYelpApi(url);
    }

    /**
     * Retrieves Yelp reviews for a given restaurant and location.
     *
     * @param restaurantName The name of the restaurant.
     * @param location       The location of the restaurant.
     * @return A list of Yelp reviews as strings.
     */
    public List<String> getReviews(String restaurantName, String location) {
        List<String> reviewTexts = new ArrayList<>();
        List<Map<String, Object>> businesses = searchBusinesses(restaurantName, location);

        if (!businesses.isEmpty()) {
            String businessId = (String) businesses.get(0).get("id"); // Get the ID of the first business
            String reviewsEndpoint = apiUrl + "/businesses/" + businessId + "/reviews";
            Map<String, Object> reviewsResult = (Map<String, Object>) callYelpApi(reviewsEndpoint);

            if (reviewsResult != null && reviewsResult.containsKey("reviews")) {
                List<Map<String, Object>> reviews = (List<Map<String, Object>>) reviewsResult.get("reviews");
                for (Map<String, Object> review : reviews) {
                    String reviewText = (String) review.get("text");
                    reviewTexts.add(reviewText);
                }
            }
        }

        return reviewTexts;
    }

    /**
     * Helper method to call the Yelp API and retrieve data.
     *
     * @param url The URL of the Yelp API endpoint.
     * @return The response data from the Yelp API as a list of businesses (maps).
     */
    private List<Map<String, Object>> callYelpApi(String url) {
        String authHeader = "Bearer " + apiKey;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class, Collections.singletonMap("Authorization", authHeader));
        if (response != null && response.containsKey("businesses")) {
            return (List<Map<String, Object>>) response.get("businesses");
        }
        return Collections.emptyList();
    }
}
