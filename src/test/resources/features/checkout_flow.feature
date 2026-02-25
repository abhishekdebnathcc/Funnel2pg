@checkout @funnel
Feature: Funnel 2-Page Checkout Flow - Complete Business Logic

  As a customer
  I want to place an order through the 2-page funnel
  So that I can complete my purchase including any upsell offers

  Background:
    Given I navigate to the landing page

  # ═══════════════════════════════════════════════════════════════════════════════
  # HAPPY PATH: Landing → Checkout → Upsells → Thank You
  # ═══════════════════════════════════════════════════════════════════════════════

  @order @happy-path @critical
  Scenario: [SC-001] Complete full 2-page funnel order flow
    """
    BUSINESS LOGIC:
    1. Landing page: fill prospect/shipping form → Rush My Order
    2. Redirected to /checkout
    3. Select product, fill payment, submit
    4. REDIRECT CHECK:
       - If → Thank You: Verify order details
       - If → Upsell: Accept & continue
    5. LOOP through upsells until Thank You page reached
    """
    # Step 1 – Landing page prospect form
    When I fill in the prospect form with valid shipping details
    And I click Rush My Order

    # Step 2 – Checkout page
    Then I should be redirected to the checkout page
    When I select a product on the checkout page
    And I select a shipping method
    And I fill in the payment details with test card
    And I accept the terms and conditions
    And I click the complete purchase button

    # Step 3 – Post-purchase redirect check
    Then I should be taken to an upsell page or thank you page

    # Step 4 – Upsell loop
    And I navigate through any upsell pages

    # Step 5 – Final verification
    Then I should land on the thank you page

  # ═══════════════════════════════════════════════════════════════════════════════
  # VALIDATION TESTS
  # ═══════════════════════════════════════════════════════════════════════════════

  @validation @negative @form-validation
  Scenario: [SC-002] Landing page form shows validation errors when submitted empty
    """
    SCENARIO: User tries to submit landing form without filling required fields
    EXPECTATION: Form validates and shows error messages
    """
    When I click Rush My Order without filling any fields
    Then I should see required field validation errors on the landing form

  @validation @negative @payment-validation
  Scenario: [SC-003] Checkout fails with invalid card number
    """
    SCENARIO: User completes landing page but submits checkout with invalid card
    EXPECTATION: Payment processing fails with error message
    """
    When I fill in the prospect form with valid shipping details
    And I click Rush My Order
    Then I should be redirected to the checkout page
    When I select a product on the checkout page
    And I select a shipping method
    And I fill in the payment details with invalid card
    And I accept the terms and conditions
    And I click the complete purchase button
    Then I should see a payment error message

  # ═══════════════════════════════════════════════════════════════════════════════
  # SMOKE / REGRESSION
  # ═══════════════════════════════════════════════════════════════════════════════

  @smoke @regression
  Scenario: [SC-004] Verify order confirmation details on thank you page
    """
    SCENARIO: After completing full 2-page flow, verify all order details are correct
    """
    When I fill in the prospect form with valid shipping details
    And I click Rush My Order
    Then I should be redirected to the checkout page
    When I select a product on the checkout page
    And I select a shipping method
    And I fill in the payment details with test card
    And I accept the terms and conditions
    And I click the complete purchase button
    Then I should be taken to an upsell page or thank you page
    And I navigate through any upsell pages
    Then I should land on the thank you page
