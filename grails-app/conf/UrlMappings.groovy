class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
                controller(validator: { !['user'].contains(it) } )
			}
		}
        "/user/$action?/$id?" {
            controller = "myUser"
        }

		"/"(view:"/index")
		"500"(view:'/error')
	}
}
