package de.kobich.audiosolutions.core.service.mp3.id3;

/**
 * Genre constants.
 * @author ckorn
 */
public enum MP3ID3GenreType {
	Blues(0), ClassicRock(1), Country(2), Dance(3), Disco(4), Funk(5), Grunge(6), HipHop(7), Jazz(8), Metal(9), NewAge(10), Oldies(11), Other(12), Pop(
			13), RhythmAndBlues(14), Rap(15), Reggae(16), Rock(17), Techno(18), Industrial(19), Alternative(20), Ska(21), DeathMetal(22), Pranks(23), Soundtrack(
			24), EuroTechno(25), Ambient(26), TripHop(27), Vocal(28), JazzFunk(29), Fusion(30), Trance(31), Classical(32), Instrumental(33), Acid(34), House(
			35), Game(36), SoundClip(37), Gospel(38), Noise(39), AlternRock(40), Bass(41), Soul(42), Punk(43), Space(44), Meditative(45), InstrumentalPop(
			46), InstrumentalRock(47), Ethnic(48), Gothic(49), Darkwave(50), TechnoIndustrial(51), Electronic(52), PopFolk(53), Eurodance(54), Dream(
			55), SouthernRock(56), Comedy(57), Cult(58), Gangsta(59), Top40(60), ChristianRap(61), PopFunk(62), Jungle(63), NativeAmerican(64), Cabaret(
			65), NewWave(66), Psychadelic(67), Rave(68), Showtunes(69), Trailer(70), LoFi(71), Tribal(72), AcidPunk(73), AcidJazz(74), Polka(75), Retro(
			76), Musical(77), RockAndRoll(78), HardRock(79), Folk(80), FolkRock(81), NationalFolk(82), Swing(83), FastFusion(84), Bebob(85), Latin(86), Revival(
			87), Celtic(88), Bluegrass(89), Avantgarde(90), GothicRock(91), ProgressiveRock(92), PsychedelicRock(93), SymphonicRock(94), SlowRock(95), BigBand(
			96), Chorus(97), EasyListening(98), Acoustic(99), Humour(100), Speech(101), Chanson(102), Opera(103), ChamberMusic(104), Sonata(105), Symphony(
			106), BootyBass(107), Primus(108), PornGroove(109), Satire(110), SlowJam(111), Club(112), Tango(113), Samba(114), Folklore(115), Ballad(
			116), PowerBallad(117), RhythmicSoul(118), Freestyle(119), Duet(120), PunkRock(121), DrumSolo(122), Acapella(123), EuroHouse(124), DanceHall(
			125), UNKNOWN(-1);
	
	private int mp3Id;

	private MP3ID3GenreType(int mp3Id) {
		this.mp3Id = mp3Id;
	}

	/**
	 * @return the mp3Id
	 */
	public int getMp3Id() {
		return mp3Id;
	}

	/**
	 * Returns genre by mp3 id
	 * @param mp3Id
	 * @return
	 */
	public static MP3ID3GenreType getByMp3Id(int mp3Id) {
		for (MP3ID3GenreType genre : MP3ID3GenreType.values()) {
			if (genre.getMp3Id() == mp3Id) {
				return genre;
			}
		}
		return MP3ID3GenreType.Other;
	}
}
