@smoke @checkout
Feature: Funnel Checkout Flow - Smoke Test

  As a customer
  I want to place an order through the funnel checkout
  So that I can complete my purchase including any upsell pages

  Background:
    Given I navigate to the checkout page

  @order @happy-path
  Scenario: Complete full funnel order flow from checkout to thank you page
    When I select a product on the main page
    And I fill in the shipping address with valid details
    And I select a shipping method
    And I fill in the payment details with test card
    And I accept the terms and conditions
    And I click the complete purchase button
    Then I should be taken to an upsell page or thank you page
    And I navigate through any upsell pages
    Then I should land on the thank you page

  @validation @negative
  Scenario: Checkout form shows validation errors when submitted empty
    When I click the complete purchase button without filling any fields
    Then I should see required field validation errors on the form

  @validation @negative
  Scenario: Checkout fails with invalid card number
    When I select a product on the main page
    And I fill in the shipping address with valid details
    And I select a shipping method
    And I fill in the payment details with invalid card
    And I accept the terms and conditions
    And I click the complete purchase button
    Then I should see a payment error message
