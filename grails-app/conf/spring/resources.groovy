import pl.tyburski.MongoUserDetailsService

//import pl.tyburski.MyUserController

// Place your Spring DSL code here
beans = {
    userDetailsService(MongoUserDetailsService)
//    userController(pl.tyburski.MyUserController)
//    passwordEncoder(pl.tyburski.PasswordEncoder)
}
