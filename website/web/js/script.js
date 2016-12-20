
/* Please keep this file [✓] UTF-8 encoded */

jQuery(function() {
  // jQuery is .ready() - let's do stuff!
  
  
  // remove noscript class to ignore noscript fallback css
  jQuery("body").removeClass("noscript");
    
  // hide scrollToTop-Button when we're already there
  jQuery( window ).scroll(function(){
    if (jQuery( document ).scrollTop() < 100) {
      jQuery(".topButton:not(:animated)").stop().fadeOut();
    } else {
      jQuery(".topButton:not(:animated)").stop().fadeIn();
    }
  });
  jQuery( window ).scroll();
  
  
  // smooth scolling for all anchor links
  jQuery('a[href^="#"]').on('click',function (e) {
    e.preventDefault();
    var target = this.hash;
    var $target = $(target.split('#').join('#anchor-'));
    if ($target.offset() != null) {
      $('html, body').stop().animate({
        'scrollTop': ($target.offset().top + $(document).scrollTop())
      }, 900, 'swing', function () {
        window.location.hash = target;
      });
    }
  });
  
  
  // toggle tabs by JS:
  jQuery('.nav-tabs a').each(function(){
    jQuery(this).click(function(e){
      e.preventDefault();
      // toggle data-tab:
      jQuery('.nav-tabs li').removeClass('active');
      jQuery(this).parent().addClass('active');
      jQuery( ".tab-pane" ).hide();
      jQuery( "#tab-" + jQuery(this).data('tab') ).show();
    });
  });
  
  
  // make main menu items with dropdowns clickable again:
  jQuery('.dropdown-toggle').click(function(){
      location.href = jQuery(this).attr('href');
  });
  

});


