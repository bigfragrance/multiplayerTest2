package modules.entity.boss;

import modules.ctrl.ServerInputManager;
import modules.entity.player.AutoController;
import modules.entity.player.ServerPlayerEntity;

public class VisitorController extends AutoController {
    public VisitorController(ServerPlayerEntity owner, ServerInputManager inputManager) {
        super(owner, inputManager);
    }
}
