package tfc.smallerunits.mixins.screens.client;

import net.minecraft.client.gui.screen.AbstractCommandBlockScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractCommandBlockScreen.class)
public interface AbstractCommandBlockScreenAccessor {
	@Accessor("commandTextField")
	TextFieldWidget SmallerUnits_getCmdTxtField();
}
