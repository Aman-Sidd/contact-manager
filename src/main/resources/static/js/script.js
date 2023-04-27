console.log("Script is working fine...")

const toggleSidebar = ()=> {
	
	if($(".sidebar").is(":visible")){
		$(".sidebar").css("display","none");
		$(".content").css("margin-left","1%");
		$(".hamburger").css("display","inline")
	}
	else{
		$(".sidebar").css("display","block");
		$(".content").css("margin-left","20%");
		$(".hamburger").css("display","none");
	}
	
}