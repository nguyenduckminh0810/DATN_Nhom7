// error-filter.js - Global error filtering (loads before Vue)
// This script runs immediately when the page loads, before any other scripts

(function() {
  'use strict';
  
  console.log('🛡️ Initializing global error filtering...');
  
  // Store original console methods
  const originalError = console.error;
  const originalWarn = console.warn;
  
  // Override console.error globally
  console.error = function(...args) {
    const message = args.join(' ');
    
    // Filter out React errors from browser extensions
    if (message.includes('inspector.b9415ea5.js') ||
        message.includes('Minified React error') ||
        message.includes('reactjs.org/docs/error-decoder') ||
        message.includes('invariant=130') ||
        message.includes('chrome-extension://') ||
        message.includes('moz-extension://')) {
      // Completely suppress these errors
      return;
    }
    
    // Allow legitimate errors to show
    originalError.apply(console, args);
  };
  
  // Override console.warn globally
  console.warn = function(...args) {
    const message = args.join(' ');
    
    // Filter out React warnings from browser extensions
    if (message.includes('inspector.b9415ea5.js') ||
        message.includes('React') ||
        message.includes('chrome-extension://') ||
        message.includes('moz-extension://')) {
      return;
    }
    
    // Allow legitimate warnings to show
    originalWarn.apply(console, args);
  };
  
  // Global error handler
  window.onerror = function(message, source, lineno, colno, error) {
    // Suppress errors from inspector files
    if (source && (source.includes('inspector.b9415ea5.js') ||
                   source.includes('chrome-extension://') ||
                   source.includes('moz-extension://'))) {
      return true; // Prevent default error handling
    }
    return false; // Allow default error handling for legitimate errors
  };
  
  // Unhandled promise rejection handler
  window.addEventListener('unhandledrejection', function(event) {
    const message = event.reason?.message || event.reason?.toString() || '';
    
    // Suppress promise rejections from inspector files
    if (message.includes('inspector.b9415ea5.js') ||
        message.includes('Minified React error') ||
        message.includes('chrome-extension://') ||
        message.includes('moz-extension://')) {
      event.preventDefault();
      return;
    }
  });
  
  console.log('✅ Global error filtering initialized');
  console.log('🔇 React errors from browser extensions will be hidden');
  
})(); 