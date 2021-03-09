
package com.webcheckers.ui;

import com.webcheckers.application.PlayerLobby;
import com.webcheckers.model.Player;
import com.webcheckers.util.Message;
import spark.*;

import java.util.HashMap;
import java.util.Map;

public class PostSignInRoute implements Route {

    private final PlayerLobby playerLobby;
    private final TemplateEngine templateEngine;

    public PostSignInRoute(PlayerLobby playerLobby, TemplateEngine templateEngine) {
        this.playerLobby = playerLobby;
        this.templateEngine = templateEngine;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String playerName = request.queryParams("username");
        Map<String, Object> viewModel = new HashMap<>();
        viewModel.put("title", "Sign In");

        if(playerLobby.isNameTaken(playerName)) {
            viewModel.put("message", Message.error("That name is already taken"));
        } else if(!playerLobby.isValidName(playerName)) {
            viewModel.put("message", Message.error("Names must be letters, numbers, and spaces"));
        } else {
            Player currentUser = playerLobby.newPlayer(playerName);
            request.session().attribute("currentUser", currentUser);
            response.redirect("/");
        }
        return templateEngine.render(new ModelAndView(viewModel, "signin.ftl"));
    }
}
