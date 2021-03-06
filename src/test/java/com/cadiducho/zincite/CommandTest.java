package com.cadiducho.zincite;

import com.cadiducho.zincite.api.command.BotCommand;
import com.cadiducho.zincite.api.command.CommandContext;
import com.cadiducho.zincite.api.command.CommandInfo;
import com.cadiducho.zincite.api.command.args.Argument;
import com.cadiducho.zincite.api.command.args.CommandParseException;
import com.cadiducho.telegrambotapi.Chat;
import com.cadiducho.telegrambotapi.Message;
import com.cadiducho.telegrambotapi.User;
import com.cadiducho.telegrambotapi.exception.TelegramException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CommandTest {

    @Test
    public void testCommandInfo() {
        BotCommand command = new TestCommand();

        assertEquals("/comando", command.getName());
        assertIterableEquals(Arrays.asList("/comando", "/alias", "/alternativa"), command.getAliases());
        assertEquals("descripción de prueba", command.getDescription());
    }

    @Test
    public void testCommandUsage() {
        BotCommand command = new TestCommandWithArguments();

        String usoEsperado = "<code>/commandWithArgs &lt;nombre&gt; [cantidad]</code>: resumen del comando con argumentos";
        usoEsperado += "\n <b>·</b> &lt;nombre&gt; (<i>Texto</i>): Nombre del usuario";
        usoEsperado += "\n <b>·</b> [cantidad] (<i>Número</i>, opcional): Cantidad a asignar";

        assertEquals(usoEsperado, command.getUsage());
    }

    @Test
    public void testCommandArguments() {
        BotCommand command = new TestCommandWithArguments();
        CommandContext context = new CommandContext(command.getArguments(), new String[] {"John"});
        assertDoesNotThrow(() -> command.execute(null, null, context, null, null, null));
    }

    @CommandInfo(aliases = {"/comando", "/alias", "/alternativa"}, description = "descripción de prueba")
    private class TestCommand implements BotCommand {
        @Override
        public void execute(Chat chat, User from, CommandContext context, Integer messageId, Message replyingTo, Instant instant) throws TelegramException {
        }
    }

    @CommandInfo(aliases = "/commandWithArgs", description = "resumen del comando con argumentos",
            arguments = {
                    @Argument(name = "nombre", type = String.class, required = true, description = "Nombre del usuario"),
                    @Argument(name = "cantidad", type = Integer.class, required = false, description = "Cantidad a asignar")
            }
    )
    private class TestCommandWithArguments implements BotCommand {
        @Override
        public void execute(final Chat chat, final User from, final CommandContext context, final Integer messageId, final Message replyingTo, Instant instant) throws TelegramException {
            try {
                Optional<String> nombre = context.get("nombre");
                if (!nombre.isPresent()) throw new TelegramException("'nombre' is not present");
                Optional<Integer> cantidad = context.get("cantidad");
                if (cantidad.isPresent()) throw new TelegramException("'cantidad' is present");
            } catch (CommandParseException ex) {
                throw new TelegramException(ex);
            }
        }
    }
}
