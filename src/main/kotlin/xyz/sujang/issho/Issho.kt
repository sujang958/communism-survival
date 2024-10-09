package xyz.sujang.issho

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.PlayerInventory
import org.bukkit.plugin.java.JavaPlugin


class Issho : JavaPlugin(), Listener {
    val message = mutableMapOf<EntityDamageEvent.DamageCause, String>(
        EntityDamageEvent.DamageCause.FALL to "높은 높이",
        EntityDamageEvent.DamageCause.ENTITY_ATTACK to "ㅄ",
        EntityDamageEvent.DamageCause.BLOCK_EXPLOSION to "알라후아크바르",
        EntityDamageEvent.DamageCause.CAMPFIRE to "모닥불",
        EntityDamageEvent.DamageCause.SUFFOCATION to "산소가 없어서",
        EntityDamageEvent.DamageCause.DROWNING to "물은 답을 알고있다",
        EntityDamageEvent.DamageCause.FIRE to "프로메테우스",
        EntityDamageEvent.DamageCause.FREEZE to "동사(verb 아님 엌ㅋㅋㅋ)",
        EntityDamageEvent.DamageCause.LAVA to "뜨거운 돌",
        EntityDamageEvent.DamageCause.VOID to "에테르",
    )

    override fun onEnable() {
        logger.info("Hello v1")

        server.pluginManager.registerEvents(this, this)
//        server.scheduler.scheduleSyncRepeatingTask(this, Runnable {
//            server.onlinePlayers.forEach {player ->
//                val uuid = player.uniqueId.toString()
//
//                val oldInv = invhm.get(uuid)
//                val newInv = player.inventory.map { itemStack -> Base64ItemStack.encode(itemStack) }.joinToString { "--" }
//
//                if (!oldInv.equals(newInv)) {
//                    invhm.set(uuid, newInv)
//                    updateOthersInventory(player.inventory)
//                }
//            }
//        }, 0L, 1L)
    }

    override fun onDisable() {
        logger.info("Bye")
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        event.player.sendMessage("왜사냐")
    }

    @EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        event.player.sendMessage("drop")

        updateOthersInventory(event.player.inventory)
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val deadPlayer = event.player

        server.onlinePlayers.forEach { player ->
            if (player.uniqueId != deadPlayer.uniqueId) {
                player.sendHurtAnimation(80F)
                player.sendMessage("누가 뒤짐 ㅅㄱ")
                player.inventory.clear()
            }
        }
    }

    @EventHandler
    fun onInvClick(event: InventoryClickEvent) {
        if (event.whoClicked.type !== EntityType.PLAYER) return

        val player = event.whoClicked as Player

        updateOthersInventory(player)
    }

    // TODO: health, exp, effects, save

    @EventHandler
    fun onInvDrag(event: InventoryDragEvent) {
        if (event.whoClicked.type !== EntityType.PLAYER) return

        val player = event.whoClicked as Player

        updateOthersInventory(player)
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player

        updateOthersInventory(player)
    }

    @EventHandler
    fun onEquip(event: PlayerArmorChangeEvent) {
        updateOthersInventory(event.player)
    }

    @EventHandler
    fun onExp(event: PlayerExpChangeEvent) {
        server.scheduler.runTask(this, Runnable {
            server.onlinePlayers.forEach { player ->
                player.exp = event.player.exp
            }
        })
    }

    @EventHandler
    fun onHurt(event: EntityDamageEvent) {
        if (event.entityType !== EntityType.PLAYER) return
        if (event.cause == EntityDamageEvent.DamageCause.CUSTOM) return

        val criteria = event.entity as Player

        server.onlinePlayers.forEach { player ->
            if (player.uniqueId !== criteria.uniqueId)
                player.damage(event.damage)
        }

        val text = Component.text(criteria.name, NamedTextColor.AQUA)
            .append(Component.text("님이 ", NamedTextColor.WHITE))
            .append(
                Component.text(
                    event.damageSource.causingEntity?.name ?: message[event.cause] ?: event.cause.name,
                    NamedTextColor.GREEN
                )
            )
            .append(Component.text("한테 ", NamedTextColor.WHITE))
            .append(Component.text("${event.damage}", NamedTextColor.RED))
            .append(Component.text("만큼 쳐맞으셨습니다!", NamedTextColor.WHITE))

        Bukkit.broadcast(text)
    }

    @EventHandler
    fun onBreak(event: PlayerItemBreakEvent) {
        updateOthersInventory(event.player)
    }

    @EventHandler
    fun onConsume(event: PlayerItemConsumeEvent) {
        logger.info("Consume")

        updateOthersInventory(event.player)
    }

    @EventHandler
    fun onPlace(event: BlockPlaceEvent) {
        updateOthersInventory(event.player)
    }

    @EventHandler
    fun onPick(event: EntityPickupItemEvent) {
        if (event.entityType !== EntityType.PLAYER) return

        val player = event.entity as Player

        updateOthersInventory(player)
    }

    @EventHandler
    fun onSwap(event: PlayerSwapHandItemsEvent) {
        updateOthersInventory(event.player)
    }

    private fun updateOthersInventory(criteria: Player) {
        server.scheduler.runTask(this, Runnable {
            criteria.updateInventory()
            server.onlinePlayers.forEach { target -> setPlayerInventory(target, criteria.inventory) }
        })
    }

    private fun updateOthersInventory(criteria: PlayerInventory) {
        server.onlinePlayers.forEach { target -> setPlayerInventory(target, criteria) }
    }

    private fun setPlayerInventory(player: Player, inv: PlayerInventory) {
        player.inventory.contents = inv.contents
        player.inventory.extraContents = inv.extraContents
        player.inventory.armorContents = inv.armorContents

        player.updateInventory()
    }
}
