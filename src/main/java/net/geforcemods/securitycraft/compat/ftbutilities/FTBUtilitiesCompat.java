package net.geforcemods.securitycraft.compat.ftbutilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import com.feed_the_beast.ftblib.lib.data.FTBLibAPI;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;

import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.util.TeamUtils.TeamRepresentation;
import net.minecraft.entity.player.EntityPlayerMP;

public class FTBUtilitiesCompat {
	private FTBUtilitiesCompat() {}

	public static boolean areOnSameTeam(Owner owner1, Owner owner2) {
		try {
			return FTBLibAPI.arePlayersInSameTeam(UUID.fromString(owner1.getUUID()), UUID.fromString(owner2.getUUID()));
		}
		catch (IllegalArgumentException e) {
			return false;
		}
	}

	public static TeamRepresentation getTeamRepresentation(Owner owner) {
		if (Universe.loaded()) {
			try {
				ForgeTeam team = Universe.get().getPlayer(UUID.fromString(owner.getUUID())).team;

				if (team != null)
					return new TeamRepresentation(team.getTitle().getFormattedText(), team.getColor().getTextFormatting());
			}
			catch (IllegalArgumentException e) {}
		}

		return null;
	}

	public static Collection<EntityPlayerMP> getOnlinePlayersInTeam(Owner owner) {
		try {
			ForgeTeam team = Universe.get().getPlayer(UUID.fromString(owner.getUUID())).team;

			if (team != null)
				return team.getOnlineMembers();
		}
		catch (IllegalArgumentException e) {}

		return new ArrayList<>();
	}
}