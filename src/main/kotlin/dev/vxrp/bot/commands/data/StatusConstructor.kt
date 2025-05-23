package dev.vxrp.bot.commands.data

import io.github.vxrpenter.secretlab.data.Server
import dev.vxrp.bot.status.data.Instance

class StatusConstructor(
    bots: HashMap<String, Int>, servers: HashMap<Int, Server>, var instance: Instance) {
    var mappedBots: HashMap<String, Int> = bots

    var mappedServers: HashMap<Int, Server> = servers
}