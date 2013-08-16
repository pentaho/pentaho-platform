// This script ensures that no cached basic-auth credentials remain in the browser
window.addEventListener("load", function(){
	$.ajax
	  ({
	    type: "GET",
	    url: CONTEXT_PATH+"js/deny.js",
	    dataType: 'json',
	    async: false,
	    data: '{"username": "bad", "password" : "aaa"}',
	    success: function (){
	    },
	    beforeSend: function(xhr){
            xhr.setRequestHeader("Authorization", "none");
        },
	    error: function(){
	      $.ajax
		  ({
		    type: "GET",
		    url: CONTEXT_PATH+"js/deny.js",
		    dataType: 'json',
		    async: false,
		    data: '{"username": "bad", "password" : "aaa"}',
		    success: function (){
		    },
		    beforeSend: function(xhr){
	            xhr.setRequestHeader("Authorization", "Basic AA~~`A=");
	        },
		    error: function(){
		    }
		});
	    }
	});
})