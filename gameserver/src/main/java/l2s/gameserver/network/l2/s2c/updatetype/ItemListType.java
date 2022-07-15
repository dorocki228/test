/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2s.gameserver.network.l2.s2c.updatetype;

/**
 * @author UnAfraid
 */
public enum ItemListType implements IUpdateTypeComponent
{
	AUGMENT_BONUS(1 << 0),
	ELEMENTAL_ATTRIBUTE(1 << 1),
	ENCHANT_EFFECT(1 << 2),
	VISUAL_ID(1 << 3),
	SOUL_CRYSTAL(1 << 4),
	UNK1(1 << 5),
	REUSE_DELAY(1 << 6),
	UNK2(1 << 7);

	private final int _mask;
	
	private ItemListType(int mask)
	{
		_mask = mask;
	}
	
	@Override
	public int getMask()
	{
		return _mask;
	}
}
