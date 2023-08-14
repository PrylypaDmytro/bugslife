$(document).ready(function () {
  // Initialize tooltips with "click" trigger
  var tooltipTriggerList = [].slice.call(
    document.querySelectorAll('[data-bs-toggle="tooltip"]')
  );
  var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
    return new bootstrap.Tooltip(tooltipTriggerEl, {
      trigger: "click",
      delay: { "show": 100, "hide": 0 } // Add a slight delay to show
    });
  });

  // Add click event listener to the question mark icons
  $('[data-bs-toggle="tooltip"]').on('shown.bs.tooltip', function (event) {
    var categoryId = $(this).data('category-id'); // Extract category ID from data attribute
    var tooltipElement = this; // Store reference to the tooltip element
    // Perform AJAX request to fetch related products
    $.ajax({
      url: "/api/categories/" + categoryId + "/productRelation", // Adjust the URL accordingly
      type: "GET",
      dataType: "json",
      success: function (data) {
        var categoryProducts = data.data;
        var productNames = categoryProducts.map(function (categoryProduct) {
          return categoryProduct.name;
        }).join(', ');

        // Update the tooltip content with the fetched product names
        $(tooltipElement).attr('title', '現在の紐付け商品: ' + productNames);
      },
      error: function (xhr, status, error) {
        console.error('Error fetching product names:', error);
      }
    });
  });
});
