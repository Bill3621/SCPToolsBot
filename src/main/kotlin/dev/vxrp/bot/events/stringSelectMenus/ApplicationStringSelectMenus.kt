package dev.vxrp.bot.events.stringSelectMenus

import dev.vxrp.bot.application.ApplicationMessageHandler
import dev.vxrp.bot.modals.ApplicationTemplateModals
import dev.vxrp.configuration.data.Config
import dev.vxrp.configuration.data.Translation
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent

class ApplicationStringSelectMenus(val event: StringSelectInteractionEvent, val config: Config, val translation: Translation) {
    fun init() {
        if (event.selectMenu.id?.startsWith("application_activation_add") == true) {
            event.replyModal(ApplicationTemplateModals(translation).chooseCountModal(event.selectedOptions[0].value, event.selectMenu.id?.split(":")?.get(2)!!)).queue()
        }

        if (event.selectMenu.id?.startsWith("application_activation_remove") == true) {
            val roleId = event.selectedOptions[0].value
            val messageId = event.selectMenu.id!!.split(":")[2]

            event.deferEdit().queue()
            ApplicationMessageHandler(config, translation).editActivationMessage(event.user.id, roleId, event.channel.asTextChannel(), messageId, state = false, member = 0)
        }
    }
}