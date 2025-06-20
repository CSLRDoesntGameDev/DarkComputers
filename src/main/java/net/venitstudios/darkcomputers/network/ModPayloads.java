package net.venitstudios.darkcomputers.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.venitstudios.darkcomputers.DarkComputers;
import net.venitstudios.darkcomputers.block.entity.custom.ComputerBlockEntity;
import net.venitstudios.darkcomputers.block.entity.custom.TerminalBlockEntity;
import net.venitstudios.darkcomputers.screen.custom.terminal.TerminalScreen;

import java.nio.charset.StandardCharsets;

public class ModPayloads {

    public record cmpUpdate(BlockPos blockPos, byte[] charBuffer, int numCycles) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<cmpUpdate> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(
                DarkComputers.MOD_ID, "cmp_display_update"));
        public static final StreamCodec<ByteBuf, cmpUpdate> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.fromCodec(BlockPos.CODEC), cmpUpdate::blockPos,  ByteBufCodecs.BYTE_ARRAY, cmpUpdate::charBuffer, ByteBufCodecs.INT, cmpUpdate::numCycles, cmpUpdate::new);
        @Override  public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record cpuResetReq(BlockPos blockPos) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<cpuResetReq> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(
                DarkComputers.MOD_ID, "cpu_reset_request"));
        public static final StreamCodec<ByteBuf, cpuResetReq> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.fromCodec(BlockPos.CODEC), cpuResetReq::blockPos, cpuResetReq::new);
        @Override  public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    public record ioKeyAction(BlockPos blockPos, int keyCode, int modifiers, boolean pressed) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ioKeyAction> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(
                DarkComputers.MOD_ID, "server_io_key_press"));
        public static final StreamCodec<ByteBuf, ioKeyAction> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.fromCodec(BlockPos.CODEC), ioKeyAction::blockPos,
                ByteBufCodecs.INT, ioKeyAction::keyCode, ByteBufCodecs.INT,
                ioKeyAction::modifiers, ByteBufCodecs.BOOL, ioKeyAction::pressed, ioKeyAction::new);
        @Override  public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record ioCharType(BlockPos blockPos, int codePoint, int modifiers) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<ioCharType> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(
                DarkComputers.MOD_ID, "server_io_char_typed"));
        public static final StreamCodec<ByteBuf, ioCharType> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.fromCodec(BlockPos.CODEC), ioCharType::blockPos,
                ByteBufCodecs.INT, ioCharType::codePoint, ByteBufCodecs.INT,
                ioCharType::modifiers, ioCharType::new);
        @Override  public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record fileStatusUpdate(BlockPos blockPos, String fileList, String currentFile, boolean editingFile, ItemStack itemStack) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<fileStatusUpdate> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(
                DarkComputers.MOD_ID, "server_update_terminal_file_status"));
        public static final StreamCodec<ByteBuf, fileStatusUpdate> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.fromCodec(BlockPos.CODEC), fileStatusUpdate::blockPos,
                ByteBufCodecs.STRING_UTF8, fileStatusUpdate::fileList,
                ByteBufCodecs.STRING_UTF8, fileStatusUpdate::currentFile,
                ByteBufCodecs.BOOL, fileStatusUpdate::editingFile,
                ByteBufCodecs.fromCodec(ItemStack.CODEC), fileStatusUpdate::itemStack,
                fileStatusUpdate::new);
        @Override  public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record terminalFileContentUpdate(BlockPos blockPos, byte[] content) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<terminalFileContentUpdate> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(
                DarkComputers.MOD_ID, "server_update_terminal_file_content"));
        public static final StreamCodec<ByteBuf, terminalFileContentUpdate> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.fromCodec(BlockPos.CODEC), terminalFileContentUpdate::blockPos,
                ByteBufCodecs.BYTE_ARRAY, terminalFileContentUpdate::content,
                terminalFileContentUpdate::new);
        @Override  public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    
    public record terminalCursorUpdate(BlockPos blockPos, int curCol, int curRow, int curRowOffset, int curColOffset) implements CustomPacketPayload {
        public static final CustomPacketPayload.Type<terminalCursorUpdate> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(
                DarkComputers.MOD_ID, "server_update_terminal_cursor"));
        public static final StreamCodec<ByteBuf, terminalCursorUpdate> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.fromCodec(BlockPos.CODEC), terminalCursorUpdate::blockPos,
                ByteBufCodecs.INT, terminalCursorUpdate::curCol,
                ByteBufCodecs.INT, terminalCursorUpdate::curRow,
                ByteBufCodecs.INT, terminalCursorUpdate::curRowOffset,
                ByteBufCodecs.INT, terminalCursorUpdate::curColOffset,
                terminalCursorUpdate::new);
        @Override  public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    @SubscribeEvent // https://docs.neoforged.net/docs/1.21.1/networking/payload
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        // Sets the current network version
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                ModPayloads.cmpUpdate.TYPE,
                ModPayloads.cmpUpdate.STREAM_CODEC,
                ModPayloads::clientCmpDisplayUpdate
        );

        registrar.playToClient(
                fileStatusUpdate.TYPE,
                fileStatusUpdate.STREAM_CODEC,
                ModPayloads::clientTermFileUpdate
        );

        registrar.playToClient(
                terminalFileContentUpdate.TYPE,
                terminalFileContentUpdate.STREAM_CODEC,
                ModPayloads::clientFileContentUpdate
        );

        registrar.playToClient(
                ModPayloads.terminalCursorUpdate.TYPE,
                ModPayloads.terminalCursorUpdate.STREAM_CODEC,
                ModPayloads::clientTermCursorUpdate
        );

        registrar.playToServer(
                ModPayloads.cpuResetReq.TYPE,
                ModPayloads.cpuResetReq.STREAM_CODEC,
                ModPayloads::serverCpuResetReq
        );

        registrar.playToServer(
                ioKeyAction.TYPE,
                ioKeyAction.STREAM_CODEC,
                ModPayloads::serverIOKeyPress
        );

        registrar.playToServer(
                ioCharType.TYPE,
                ioCharType.STREAM_CODEC,
                ModPayloads::serverIOCharTyped
        );

    }

    @OnlyIn(Dist.CLIENT)
    private static void clientCmpDisplayUpdate(final ModPayloads.cmpUpdate packet, final IPayloadContext context) {
        BlockEntity blockEntity = context.player().level().getBlockEntity(packet.blockPos);
        if (blockEntity instanceof ComputerBlockEntity computer) {
            byte[] packetCharBuffer = packet.charBuffer;
            String pcbString = new String(packetCharBuffer, StandardCharsets.UTF_8);
            computer.bus.displayDriver.screenBuffer = pcbString.toCharArray();
            computer.bus.cyclesRan = packet.numCycles;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void clientTermFileUpdate(final fileStatusUpdate packet, final IPayloadContext context) {
        BlockEntity blockEntity = context.player().level().getBlockEntity(packet.blockPos);
        if (blockEntity instanceof TerminalBlockEntity terminal) {
            if (terminal.editor.currentScreen instanceof TerminalScreen) {
                terminal.editor.currentScreen.files = packet.fileList.split(",");
                terminal.storageStack = packet.itemStack;
                terminal.editingFile = packet.editingFile;
                terminal.editor.currentFileName = packet.currentFile;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void clientTermCursorUpdate(final terminalCursorUpdate packet, final IPayloadContext context) {
        BlockEntity blockEntity = context.player().level().getBlockEntity(packet.blockPos);

        if (blockEntity instanceof TerminalBlockEntity terminal) {

            if (terminal.editor.currentScreen instanceof TerminalScreen) {

                terminal.editor.curCol = packet.curCol;
                terminal.editor.curRow = packet.curRow;

                terminal.editor.curColOffset = packet.curColOffset;
                terminal.editor.curRowOffset = packet.curRowOffset;

            }

        }

    }

    @OnlyIn(Dist.CLIENT)
    private static void clientFileContentUpdate(final terminalFileContentUpdate packet, final IPayloadContext context) {
        BlockEntity blockEntity = context.player().level().getBlockEntity(packet.blockPos);
        if (blockEntity instanceof TerminalBlockEntity terminal) {
            terminal.editor.loadBytesToEdit(packet.content);
        }
    }

    private static void serverCpuResetReq(final ModPayloads.cpuResetReq packet, final IPayloadContext context) {
        BlockEntity blockEntity = context.player().level().getBlockEntity(packet.blockPos);
        if (blockEntity instanceof ComputerBlockEntity computer) {
            computer.bus.resetBus();
            computer.bus.processor.resetProcessor();
        }
    }

    private static void serverIOCharTyped(final ioCharType packet, final IPayloadContext context) {
        BlockEntity blockEntity = context.player().level().getBlockEntity(packet.blockPos);
        if (blockEntity instanceof TerminalBlockEntity terminal)  terminal.editor.charTyped((char) packet.codePoint, packet.modifiers);
    }

    private static void serverIOKeyPress(final ioKeyAction packet, final IPayloadContext context) {
        BlockEntity blockEntity = context.player().level().getBlockEntity(packet.blockPos);
        if (blockEntity instanceof ComputerBlockEntity computer)  computer.bus.updateKeyState(packet.keyCode, packet.modifiers, packet.pressed);
        if (blockEntity instanceof TerminalBlockEntity terminal)  terminal.editor.keyPressed(packet.keyCode, 0, packet.modifiers);
    }
}
