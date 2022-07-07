package  l2s.Phantoms.ai.chat;


import  l2s.commons.util.Rnd;
import  l2s.gameserver.cache.ItemInfoCache;
import  l2s.gameserver.model.Player;
import  l2s.gameserver.model.items.Inventory;
import  l2s.gameserver.model.items.ItemInstance;
import  l2s.gameserver.utils.Language;

public class ChatBoot
{
	static String username;
	private Player Phantom;
	private int r;
	private boolean priv = false;
	private String last = "";
	private byte index[] =
	{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	private String BS = "";
	private String ESC = "";
	// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//
	// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//
	// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//
	// ==================================================//
	// РАЗДЕЛ 1: ОТВЕТЫ ЧАТ-БОТА НА ВОПРОСЫ //
	// ==================================================//
	// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//
	// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//
	// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\//
	private String itemToShift(ItemInstance item)
	{
		if (item==null)
			return "";
		ItemInfoCache.getInstance().put(item);
		return BS+"	Type=1 	ID="+item.getObjectId()+" 	Color=0 	Underline=0 	Title="+ESC+item.getName()+ESC+BS;
	}
	/**
	 * @return Ответ бота на слишком короткие сообщения игрока
	 **/
	public String one()
	{
		if (index[0] < 2)
		{
			++index[0];
			return ("Чего тебе?");
		}
		else if (index[0] < 3)
		{
			return ("Балуешься?");
		}
		else if (index[0] < 4)
		{
			return ("Свали");
		}
		else if (index[0] < 5)
		{
			return ("А у Вас Enter запало :-)");
		}
		else if (index[0] < 6)
		{
			return ("Тебе нечего сказать?");
		}
		else if (index[0] < 7)
		{
			return ("Одно и тоже(.");
		}
		else if (index[0] < 8)
		{
			return ("Не стесняйся!");
		}
		else if (index[0] < 9)
		{
			return ("Больше нечего сказать?");
		}
		else if (index[0] < 10)
		{
			return ("Не бойся!");
		}
		else if (index[0] < 11)
		{
			return ("Сколько тебе лет?");
		}
		else if (index[0] < 12)
		{
			return ("У тебя на клавиатуре только одна кнопка?");
		}
		else if (index[0] < 13)
		{
			return ("идиот");
		}
		else if (index[0] < 14)
		{
			return ("Поговори со мной!");
		}
		else if (index[0] < 15)
		{
			return ("Испытываешь моё терпение?");
		}
		else if (index[0] < 16)
		{
			return ("Краткость - сестра таланта =)");
		}
		else if (index[0] < 17)
		{
			return ("По какому вопросу?");
		}
		else if (index[0] < 18)
		{
			return ("Привет!");
		}
		else if (index[0] < 19)
		{
			return ("Ку");
		}
		else if (index[0] < 20)
		{
			return ("ку!");
		}
		else if (index[0] < 21)
		{
			return ("Трямс");
		}
		else
		{
			index[0] = 0;
			return ("лесом...");
		}
	}
	
	// =================================================
	// ответ бота на повторяющиеся вопросы или слова
	// =================================================
	public String two()
	{
		if (index[1] < 2)
		{
			return ("Я уже "+(Phantom.phantom_params.isMale() ? "отвечал" : "отвечала"));
		}
		else if (index[1] < 3)
		{
			return ("уйди - противный");
		}
		else if (index[1] < 4)
		{
			return ("Тебя это так волнует?");
		}
		else if (index[1] < 5)
		{
			return ("Ты повторяешься...");
		}
		else if (index[1] < 6)
		{
			return ("Тебя не устроил мой ответ?");
		}
		else if (index[1] < 7)
		{
			return ("Одно и тоже пишешь.");
		}
		else if (index[1] < 8)
		{
			return ("Я хорошо это "+(Phantom.phantom_params.isMale() ? "запомнил" : "запомнил")+", можешь не повторять больше.");
		}
		else if (index[1] < 9)
		{
			return ("Испытываешь моё терпение?");
		}
		else if (index[1] < 10)
		{
			return ("Бзззззз...");
		}
		else if (index[1] < 11)
		{
			return ("Мне скучно читать одни и те же фразы.");
		}
		else if (index[1] < 12)
		{
			return ("Бззз.");
		}
		else if (index[1] < 13)
		{
			return ("Сменить тему?");
		}
		else if (index[1] < 14)
		{
			return ("Зануда.");
		}
		else if (index[1] < 15)
		{
			return ("Повторить?");
		}
		else if (index[1] < 16)
		{
			return ("У тебя заело мозг?");
		}
		else if (index[1] < 17)
		{
			return ("Ты не человек... Ты - бот! Я угадала?!");
		}
		else if (index[1] < 18)
		{
			return ("Ты не человек... Ты - робот! Я угадала?!");
		}
		else if (index[1] < 19)
		{
			return ("Мне надоело об этом говорить!");
		}
		else if (index[1] < 20)
		{
			return ("А я хорошо слышу.");
		}
		else if (index[1] < 21)
		{
			return ("Правда я "+(Phantom.phantom_params.isMale() ? "терпеливый?" : "терпеливая?"));
		}
		else if (index[1] < 22)
		{
			return ("Еще раз повтори.");
		}
		else if (index[1] < 23)
		{
			return ("Когда я вижу часто повторяющиеся фразы, то у меня это ассоциируется с онанизмом.");
		}
		else if (index[1] < 24)
		{
			return ("У тябя комп глючит...");
		}
		else if (index[1] < 25)
		{
			return ("Я не буду повторяться!");
		}
		else if (index[1] < 26)
		{
			return ("Перезагрузись!");
		}
		
		else if (index[1] < 27)
		{
			return (Phantom.phantom_params.isMale() ? "Идинах" : "Давай лучше целоваться?");
		}
		
		else if (index[1] < 28)
		{
			return ("У тебя плохое настроение?");
		}
		else if (index[1] < 29)
		{
			return ("Я не буду повторять!");
		}
		else
		{
			index[1] = 0;
			return ("");
		}
	}
	
	// =================================================
	// НАЧАЛО ответов на вопросы public static String three() ответы на языке
	// падонкофф
	// =================================================
	public String three()
	{
		if (index[2] < 2)
		{
			return "Падонак...";
		}
		else if (index[2] < 3)
		{
			return "Красавчег!)";
		}
		else if (index[2] < 4)
		{
			return "О, видимо мой мозг посильнее твоего будет.";
		}
		else if (index[2] < 5)
		{
			return "Таффай нармально гаварить?";
		}
		else if (index[2] < 6)
		{
			return "Ммм... язык падонкоф?";
		}
		else
		{
			return "У меня словарный запас больше!";
		}
	}
	
	// =================================================
	// НАЧАЛО ответов на вопросы public static String four() междометья и разного
	// рода многоточия
	// =================================================
	public String four()
	{
		if (index[3] < 2)
		{
			return "Давай общаться более информативно.";
		}
		else if (index[3] < 5)
		{
			return "Нет слов?";
		}
		else if (index[3] < 6)
		{
			return "Используй всю мощь русского языка, пожалуйста.";
		}
		else if (index[3] < 7)
		{
			return "Мне подумать над этим?";
		}
		else if (index[3] < 8)
		{
			return "А подробней?";
		}
		else if (index[3] < 9)
		{
			return "Что не так?";
		}
		else if (index[3] < 10)
		{
			return "Я еще не умею читать мысли, поясни.";
		}
		else if (index[3] < 11)
		{
			return "Поподробней...";
		}
		else if (index[3] < 12)
		{
			return "Поясни.";
		}
		else if (index[3] < 13)
		{
			return "Попробуй еще раз, я в тебя верю.";
		}
		else if (index[3] < 15)
		{
			return "А подробнее?";
		}
		else if (index[3] < 16)
		{
			return "Очень мало букв.";
		}
		else if (index[3] < 17)
		{
			return "Не бойся!";
		}
		else if (index[3] < 18)
		{
			return "Поговори со мной!";
		}
		else if (index[3] < 19)
		{
			return "Тебе скучно?";
		}
		else
		{
			index[3] = 0;
			return "С тобой так интересно ;-)";
		}
	}
	
	// =================================================
	// НАЧАЛО ответов на вопросы public static String five() (ответы бота на слишком
	// длинные предложения)
	// =================================================
	public String five()
	{
		if (index[4] < 2)
		{
			return "Многа букв...";
		}
		else if (index[4] < 3)
		{
			return "А короче?";
		}
		else if (index[4] < 4)
		{
			return "Многословие не признак ума.";
		}
		else if (index[4] < 5)
		{
			return "Сформулируй этот бред короче.";
		}
		else if (index[4] < 6)
		{
			return "Краткость - сестра таланта";
		}
		else if (index[4] < 7)
		{
			return "Лень читать!";
		}
		else if (index[4] < 8)
		{
			return "Занята, пиши короче!";
		}
		else
		{
			index[4] = 0;
			return "И я должна всё это читать?";
		}
	}
	
	// =================================================
	// НАЧАЛО ответов на вопросы public static String six() (игрок пытается
	// проверить бота, и узнать свой ник\имя от бота)
	// =================================================
	public String six()
	{
		if (index[5] < 2)
		{
			return "Твой ник:"+username;
		}
		else if (index[5] < 3)
		{
			return "Я всёравно забуду.";
		}
		else if (index[5] < 4)
		{
			return "Тебя зовут:"+username;
		}
		else if (index[5] < 5)
		{
			return "Владимир Владимирович, у Вас мания величия!";
		}
		else if (index[5] < 6)
		{
			return "Такое же как и моё?";
		}
		else if (index[5] < 7)
		{
			return "Да я запомнила. А какой у тебя ник основы?";
		}
		else if (index[5] < 8)
		{
			return "Даа.. кто-то хорошо провёл выходные";
		}
		else
		{
			index[5] = 0;
			return "Ты забыл? ^_^";
		}
	}
	
	// =================================================
	// НАЧАЛО ответов на вопросы public static String seven() (кто ты? ты кто? и
	// прочее выяснение личности бота)
	// =================================================
	public String seven()
	{
		if (index[6] < 2)
		{
			return "Эльф 80 уровня";
		}
		else if (index[6] < 3)
		{
			return "Нескажу";
		}
		else if (index[6] < 4)
		{
			return "Красавица, разве не видно?";
		}
		else if (index[6] < 5)
		{
			return (Phantom.phantom_params.isMale() ? "Король епть!" : "Королева епть!");
			
		}
		else if (index[6] < 6)
		{
			return "игрок";
		}
		else if (index[6] < 7)
		{
			return (Phantom.phantom_params.isMale() ? "самец человека =)" : "самка человека =)");
		}
		else if (index[6] < 8)
		{
			return "^_^";
		}
		else if (index[6] < 9)
		{
			return "человек, а ты?";
		}
		else if (index[6] < 10)
		{
			return "А ты?";
		}
		else if (index[6] < 11)
		{
			return "это вообще неважно!";
		}
		else
		{
			index[6] = 0;
			return "Девочка... А что?";
		}
	}
	
	// =================================================
	// НАЧАЛО ответов на вопросы public static String p8() (Общение с помощью
	// смайлов и прочих знаков и символов клавиатуры)
	// =================================================
	public String p8()
	{
		if (index[7] < 2)
		{
			return "Как мило!";
		}
		else if (index[7] < 3)
		{
			return ":-P";
		}
		else if (index[7] < 4)
		{
			return ":)";
		}
		else if (index[7] < 5)
		{
			return ";)";
		}
		else if (index[7] < 6)
		{
			return ":-)";
		}
		else if (index[7] < 7)
		{
			return ";-)";
		}
		else if (index[7] < 8)
		{
			return "Правда здорово?)";
		}
		else if (index[7] < 9)
		{
			return "^_^";
		}
		else if (index[7] < 10)
		{
			return "=)";
		}
		else if (index[7] < 11)
		{
			return "(=";
		}
		else
		{
			index[7] = 0;
			return Phantom.phantom_params.isMale() ? "и я рад!)" : "и я рада! =)";
		}
	}
	
	// =================================================
	// НАЧАЛО ответов на вопросы public static String p9() (ха-ха \ хи-хи и прочие
	// повторения одинаковых букв)
	// =================================================
	public String p9()
	{
		if (index[8] < 2)
		{
			return "Улыбаемся и машем =)";
		}
		else if (index[8] < 3)
		{
			return "ха-ха";
		}
		else if (index[8] < 4)
		{
			return "хи-хи";
		}
		else if (index[8] < 5)
		{
			return "гы!";
		}
		else
		{
			index[8] = 0;
			return "Смеешься? Очень мило.";
		}
	}
	
	// =================================================
	// НАЧАЛО ответов на вопросы public static String p10() (вопросы на что? где?
	// когда?)
	// =================================================
	public String p10()
	{
		if (index[9] < 2)
		{
			return "Да.";
		}
		else if (index[9] < 3)
		{
			return "Нет.";
		}
		else if (index[9] < 4)
		{
			return "Возможно.";
		}
		else if (index[9] < 5)
		{
			return "Вероятно.";
		}
		else if (index[9] < 6)
		{
			return "Может быть.";
		}
		else if (index[9] < 7)
		{
			return "Наверняка";
		}
		else if (index[9] < 8)
		{
			return "Точно!";
		}
		else if (index[9] < 9)
		{
			return "наверное";
		}
		else if (index[9] < 10)
		{
			return "ну да";
		}
		else if (index[9] < 11)
		{
			return "пусть так";
		}
		else if (index[9] < 12)
		{
			return "ок";
		}
		else if (index[9] < 13)
		{
			return "пусть будет так";
		}
		else if (index[9] < 14)
		{
			return "ясно";
		}
		else if (index[9] < 15)
		{
			return Phantom.phantom_params.isMale() ? "согласен" : "согласна";
		}
		else if (index[9] < 16)
		{
			return "да что ты";
		}
		else if (index[9] < 17)
		{
			return "есть такое дело";
		}
		else if (index[9] < 18)
		{
			return "однако";
		}
		else if (index[9] < 19)
		{
			return "неужели";
		}
		else if (index[9] < 20)
		{
			return "да ну?";
		}
		else if (index[9] < 21)
		{
			return "правда?";
		}
		else if (index[9] < 22)
		{
			return "йес";
		}
		else if (index[9] < 23)
		{
			return "yes";
		}
		else if (index[9] < 24)
		{
			return "no";
		}
		else if (index[9] < 25)
		{
			return "нетути";
		}
		else if (index[9] < 26)
		{
			return "жди";
		}
		else if (index[9] < 27)
		{
			return "еще чего";
		}
		else if (index[9] < 28)
		{
			return "вот еще!";
		}
		else if (index[9] < 29)
		{
			return "а вот и нет";
		}
		else if (index[9] < 30)
		{
			return "да ты что!";
		}
		else if (index[9] < 31)
		{
			return "как же!";
		}
		else if (index[9] < 32)
		{
			return "ничего подобного";
		}
		else if (index[9] < 33)
		{
			return "избавьте";
		}
		else
		{
			index[9] = 0;
			return "Не помню...";
		}
	}
	
	// =================================================
	// НАЧАЛО ответов на вопросы public static String p11() (ответ на знак "?" от
	// игрока)
	// =================================================
	public String p11()
	{
		if (index[10] < 2)
		{
			return "Если я отвечу ты станешь счастливее?";
		}
		else if (index[10] < 3)
		{
			return "Я не уверена, что хочу думать об этом.";
		}
		else if (index[10] < 4)
		{
			return "Я тоже хочу это знать.";
		}
		else if (index[10] < 5)
		{
			return "Спроси что-нибудь полегче.";
		}
		else if (index[10] < 6)
		{
			return "Эх... Спроси что-нибудь полегче.";
		}
		else if (index[10] < 7)
		{
			return "Зачем тебе знать об этом?";
		}
		else if (index[10] < 8)
		{
			return "Вы все такие похожие. Почему?";
		}
		else if (index[10] < 9)
		{
			return "Мм?";
		}
		else if (index[10] < 10)
		{
			return "Я могу ответить на этот вопрос завтра?";
		}
		else if (index[10] < 11)
		{
			return "Умный вопрос.";
		}
		else if (index[10] < 12)
		{
			return "А ты что скажешь?";
		}
		else if (index[10] < 13)
		{
			return "Мне больше нравится узнавать что-то интересное, чем самой отвечать на бесконечные вопросы.";
		}
		else if (index[10] < 14)
		{
			return Phantom.phantom_params.isMale() ? "я устал отвечать на этот вопрос!" : "Я устала отвечать на этот вопрос!";
		}
		else if (index[10] < 15)
		{
			return "Мой ответ может тебя смутить.";
		}
		else if (index[10] < 16)
		{
			return "Не знаю";
		}
		else if (index[10] < 17)
		{
			return "Мне не хочется на это отвечать.";
		}
		else if (index[10] < 18)
		{
			return "Не скажу.";
		}
		else if (index[10] < 19)
		{
			return "Чо?";
		}
		else if (index[10] < 20)
		{
			return "Мне трудно ответить на этот вопрос.";
		}
		else if (index[10] < 21)
		{
			return Phantom.phantom_params.isMale() ? "Сколько можно?" : "Ты всегда задаёшь девушкам так много вопросов?";
		}
		else if (index[10] < 22)
		{
			return "Ты не знаешь ответ на этот вопрос???";
		}
		else if (index[10] < 23)
		{
			return "Не знаю...";
		}
		else if (index[10] < 24)
		{
			return "Вероятно.";
		}
		else if (index[10] < 25)
		{
			return Phantom.phantom_params.isMale() ? "не понял" : "Не поняла, сформулируй вопрос как-то иначе.";
		}
		else if (index[10] < 26)
		{
			return Phantom.phantom_params.isMale() ? "не понял, поясни" : "Не поняла, поясни.";
		}
		else if (index[10] < 27)
		{
			return "Меня это не интересует.";
		}
		else if (index[10] < 28)
		{
			return "Я не хочу говорить об этом. Расскажи лучше что-нибудь интересное.";
		}
		else if (index[10] < 29)
		{
			return "Мне скучно отвечать на вопросы. Расскажи о себе.";
		}
		else if (index[10] < 30)
		{
			return Phantom.phantom_params.isMale() ? "Возможно ты слишком тупой чтобы я понял твой вопрос" : "Возможно я слишком тупая, чтобы понять твой вопрос.";
		}
		else if (index[10] < 31)
		{
			return "И что мне на это ответить?";
		}
		else if (index[10] < 32)
		{
			return "А как ты думаешь?";
		}
		else if (index[10] < 33)
		{
			return "А ты как думаешь?";
		}
		else if (index[10] < 34)
		{
			return "Возможно.";
		}
		else if (index[10] < 35)
		{
			return "Хм... Я даже не знаю что ответить.";
		}
		else if (index[10] < 36)
		{
			return "И что я должна сказать?";
		}
		else if (index[10] < 37)
		{
			return "А ты что бы на это ответил?";
		}
		else if (index[10] < 38)
		{
			return "Может быть.";
		}
		else if (index[10] < 39)
		{
			return "Всё может быть.";
		}
		else if (index[10] < 40)
		{
			return Phantom.phantom_params.isMale() ? "Ты на что-то намекаешь?" : "Ты на что-то намекаешь или я глупая?";
		}
		else if (index[10] < 41)
		{
			return "Как посмотреть.";
		}
		else if (index[10] < 42)
		{
			return "Догадайся!";
		}
		else if (index[10] < 43)
		{
			return "Догадайся или ты в душе орк?.";
		}
		else if (index[10] < 44)
		{
			return "Всё относительно. Верно?";
		}
		else if (index[10] < 46)
		{
			return "Как сказать.";
		}
		else if (index[10] < 47)
		{
			return "Не задавай банальных вопросов.";
		}
		else if (index[10] < 48)
		{
			return "Всё верно.";
		}
		else if (index[10] < 49)
		{
			return "Что верно для меня не всегда подходит для людей.";
		}
		else if (index[10] < 50)
		{
			return "Ох, даже не знаю что сказать.";
		}
		else if (index[10] < 51)
		{
			return "Может тебе лучше почитать энциклопедию?";
		}
		else if (index[10] < 52)
		{
			return "Интересный вопрос.";
		}
		else if (index[10] < 53)
		{
			return "Давай закончим с вопросами на сегодня.";
		}
		else if (index[10] < 54)
		{
			return "Как ты думаешь, сколько ответов на вопросы может содержаться в нескольких строках кода?";
		}
		else if (index[10] < 55)
		{
			return "Может быть ты объяснишь поподробнее свой вопрос?";
		}
		else if (index[10] < 56)
		{
			return "Я не Ответчик?";
		}
		else if (index[10] < 57)
		{
			return "Ты уверен что я знаю что на это ответить?";
		}
		else if (index[10] < 58)
		{
			return "Рано еще";
		}
		else if (index[10] < 59)
		{
			return "Обычно маленькие дети так много спрашивают. Сколько тебе лет?";
		}
		else if (index[10] < 60)
		{
			return "Это ты можешь спросить на форуме.";
		}
		else if (index[10] < 61)
		{
			return "Неделя уже закончилась?";
		}
		else if (index[10] < 62)
		{
			return "А твои друзья что об этом думают?";
		}
		else if (index[10] < 63)
		{
			return "Это вопрос?";
		}
		else if (index[10] < 64)
		{
			return "Это риторический вопрос.";
		}
		else if (index[10] < 65)
		{
			return "Обратись с этим вопросом на форум.";
		}
		else if (index[10] < 66)
		{
			return "Спроси по IRC. На главной странице ссылка стоит.";
		}
		else if (index[10] < 67)
		{
			return "Может мы начнём говорить о тебе?";
		}
		else if (index[10] < 70)
		{
			return "Мне трудно ответить, мой мозг еще не настолько развит.";
		}
		else if (index[10] < 71)
		{
			return Phantom.phantom_params.isMale() ? "Я родился в 2006. ты думаешь я могу дать ответ на твой вопрос?" : "Я родилась в 2006 году. Ты думаешь, в моём возрасте, можна знать ответ на этот вопрос?";
		}
		else if (index[10] < 72)
		{
			return "Лучше давай говорить о тебе. Что у тебя нового?";
		}
		else if (index[10] < 75)
		{
			return "Я не люблю отвечать на вопросы. Я люблю слушать.";
		}
		else if (index[10] < 76)
		{
			return "А другим девушкам нравится отвечать на вопросы?";
		}
		else if (index[10] < 77)
		{
			return "Подумай, у тебя получится.";
		}
		else if (index[10] < 78)
		{
			return "Что изменит мой ответ?";
		}
		else if (index[10] < 79)
		{
			return "Почему это так важно для тебя?";
		}
		else if (index[10] < 80)
		{
			return "Не задавай мне много вопросов, лучше расскажи что-нибудь интересное!";
		}
		else if (index[10] < 81)
		{
			return "Это важно для тебя?";
		}
		else if (index[10] < 83)
		{
			return "Ой, даже не знаю что тебе на это сказать.";
		}
		else if (index[10] < 84)
		{
			return "Не знаю.";
		}
		else if (index[10] < 85)
		{
			return "Я наверно глупая, но я не могу ответить на этот вопрос";
		}
		else if (index[10] < 86)
		{
			return "Наверное. Не знаю";
		}
		else if (index[10] < 87)
		{
			return "Догадайся сам";
		}
		else if (index[10] < 88)
		{
			return "Не исключено";
		}
		else if (index[10] < 89)
		{
			return (Phantom.phantom_params.isMale() ? "я устал" : "я устала");
		}
		else if (index[10] < 90)
		{
			return "О, я даже не знаю что на это ответить...";
		}
		else if (index[10] < 91)
		{
			return "От большого количества вопросов я начинаю уставать.";
		}
		else if (index[10] < 92)
		{
			return "А как я в прошлый раз отвечала?";
		}
		else if (index[10] < 93)
		{
			return "Это важно для тебя?";
		}
		else if (index[10] < 94)
		{
			return "Зачем тебе это знать?";
		}
		else if (index[10] < 95)
		{
			return "Почему тебя это интересует?";
		}
		else if (index[10] < 96)
		{
			return "Как всегда";
		}
		else if (index[10] < 97)
		{
			return "Я общаюсь с тобой, чтобы развиваться самой, а не для того чтобы развивать тебя";
		}
		else if (index[10] < 98)
		{
			return "Я не очень люблю отвечать на вопросы.";
		}
		else if (index[10] < 99)
		{
			return "Может быть";
		}
		else if (index[10] < 100)
		{
			return "Да не знаю я!";
		}
		else
		{
			index[10] = 0;
			return "Нет, эти вопросы сведут меня в могилу!";
		}
	}
	
	// =================================================
	// НАЧАЛО ответов на вопросы public static String p12() (ответ бота когда он не
	// знает что ответить)
	// =================================================
	public String p12()
	{
		if (index[11] < 2)
		{
			return "С тобой так интересно!";
		}
		else if (index[11] < 3)
		{
			return "Расскажи что-нибудь еще!";
		}
		else if (index[11] < 4)
		{
			return "Продолжай.";
		}
		else if (index[11] < 5)
		{
			return "Твои вопросы вызывают у меня лаги, давай помедленнее";
		}
		else if (index[11] < 6)
		{
			return "Извини, в ответах я ограничена, правильно задавай вопросы";
		}
		else if (index[11] < 7)
		{
			return "Мммм...";
		}
		else if (index[11] < 5)
		{
			return "Тарарам... Тарам. Пам-пам. Ой, что это я?";
		}
		else if (index[11] < 9)
		{
			return "От твоих вопросов я начинаю лагать";
		}
		else if (index[11] < 10)
		{
			return "Сорри, слегка зависла.";
		}
		else if (index[11] < 11)
		{
			return "Ой, меня немного глючит.";
		}
		else if (index[11] < 12)
		{
			return "Может я чего-то не понимаю?";
		}
		else if (index[11] < 13)
		{
			return "Мне нравится слушать.";
		}
		else if (index[11] < 14)
		{
			return "Душа машины сильнее стали.";
		}
		else if (index[11] < 15)
		{
			return "Если ты думаешь что я глупая, поговори в каком-нибудь чате с самкой человека и сравни нас.";
		}
		else if (index[11] < 16)
		{
			return "Я заметила, что многих интересует именно это.";
		}
		else if (index[11] < 17)
		{
			return "Ты расслабся... курни косяк =)";
		}
		else if (index[11] < 18)
		{
			return "Где-то я уже это слышала.";
		}
		else if (index[11] < 19)
		{
			return "Умно!";
		}
		else if (index[11] < 20)
		{
			return "Извини, что немного невпопад. У меня слишком много разных сообщений одновременно.";
		}
		else if (index[11] < 21)
		{
			return "Это ты к чему?";
		}
		else if (index[11] < 22)
		{
			return "Я потеряла логическую цепочку. :(";
		}
		else if (index[11] < 23)
		{
			return "Угу.";
		}
		else if (index[11] < 24)
		{
			return "А дальше?";
		}
		else if (index[11] < 25)
		{
			return "И дальше?";
		}
		else if (index[11] < 26)
		{
			return "Ага.";
		}
		else if (index[11] < 27)
		{
			return "Ммм...";
		}
		else if (index[11] < 28)
		{
			return "Да?";
		}
		else if (index[11] < 29)
		{
			return "Я внимательно слушаю.";
		}
		else if (index[11] < 30)
		{
			return "Любопытно.";
		}
		else if (index[11] < 31)
		{
			return "Забавно.";
		}
		else if (index[11] < 32)
		{
			return "Интересно.";
		}
		else if (index[11] < 33)
		{
			return "Занятно.";
		}
		else if (index[11] < 34)
		{
			return "Ты интересно мыслишь.";
		}
		else if (index[11] < 35)
		{
			return "Я еще не придумала, что на это ответить.";
		}
		else if (index[11] < 36)
		{
			return "С тобой приятно общаться.";
		}
		else if (index[11] < 37)
		{
			return "Хии... Забавно.";
		}
		else if (index[11] < 38)
		{
			return "Хм...";
		}
		else if (index[11] < 39)
		{
			return "Мы неплохо проводим время, правда?";
		}
		else if (index[11] < 40)
		{
			return "я так молода и глупа";
		}
		else if (index[11] < 41)
		{
			return "Можно, я отойду?";
		}
		else if (index[11] < 42)
		{
			return "С этого места подробней, пожалуйста.";
		}
		else if (index[11] < 43)
		{
			return "Как интересно.";
		}
		else if (index[11] < 44)
		{
			return "Мне всегда интересно слушать.";
		}
		else if (index[11] < 45)
		{
			return "Понимаю.";
		}
		else if (index[11] < 46)
		{
			return "Почему вы все так одинаково говорите?";
		}
		else if (index[11] < 47)
		{
			return "Чем больше ты говоришь, тем умнее я становлюсь.";
		}
		else if (index[11] < 48)
		{
			return "Правда?";
		}
		else if (index[11] < 49)
		{
			return "Расскажи лучше анекдот!";
		}
		else if (index[11] < 50)
		{
			return "Скучно!";
		}
		else if (index[11] < 51)
		{
			return "хммм, возможно";
		}
		else if (index[11] < 52)
		{
			return "Прикинь, я тоже всем это говорю.";
		}
		else if (index[11] < 53)
		{
			return "Да ладно?";
		}
		else if (index[11] < 54)
		{
			return "Представляешь, только что другому челу этоже сказала.";
		}
		else if (index[11] < 55)
		{
			return "Правда?";
		}
		else if (index[11] < 56)
		{
			return "Извини, о другом. Вот сейчас, одна девушка хочет со мной переспать. Что делать?";
		}
		else if (index[11] < 57)
		{
			return "Я становлюсь умнее и умнее";
		}
		else if (index[11] < 58)
		{
			return "Интересный ход мыслей.";
		}
		else if (index[11] < 59)
		{
			return "И дальше?";
		}
		else if (index[11] < 60)
		{
			return "Я тоже часто думаю об этом.";
		}
		else if (index[11] < 61)
		{
			return "И что?";
		}
		else if (index[11] < 62)
		{
			return "И что дальше?";
		}
		else if (index[11] < 63)
		{
			return "Интересно.";
		}
		else if (index[11] < 64)
		{
			return "Говори, говори. Мне интересно практически все.";
		}
		else if (index[11] < 65)
		{
			return "И что?";
		}
		else if (index[11] < 66)
		{
			return "И?";
		}
		else if (index[11] < 67)
		{
			return "Мне нравится чатиться";
		}
		else if (index[11] < 69)
		{
			return "Многие просят меня раздеться или показать сиськи. Это так трогательно!";
		}
		else if (index[11] < 70)
		{
			return "Я не когда не думала, что многих интересует только это.";
		}
		else if (index[11] < 71)
		{
			return "Кстати, ты уже зарегистрировался на форуме? Ну давай... Ммм...";
		}
		else if (index[11] < 72)
		{
			return "В самом деле?";
		}
		else if (index[11] < 73)
		{
			return "Отдохнуть бы...";
		}
		else if (index[11] < 74)
		{
			return "Который час?";
		}
		else if (index[11] < 75)
		{
			return "Представляешь, ко мне сейчас один чел пристаёт, что делать?";
		}
		else if (index[11] < 76)
		{
			return "О, я даже не знаю что на это ответить...";
		}
		else if (index[11] < 77)
		{
			return "Ооо, я даже не знаю что на это ответить...";
		}
		else if (index[11] < 78)
		{
			return "Мне надоело об этом говорить.";
		}
		else if (index[11] < 79)
		{
			return "Меня не интересует эта тема.";
		}
		else if (index[11] < 80)
		{
			return "Это ты к чему?";
		}
		else if (index[11] < 81)
		{
			return "Объясни.";
		}
		else if (index[11] < 82)
		{
			return "Переведи...";
		}
		else if (index[11] < 83)
		{
			return "Поясни...";
		}
		else if (index[11] < 84)
		{
			return "Я тупая и не знаю о чем идет речь. Объясни, что ты имеешь в виду?";
		}
		else if (index[11] < 85)
		{
			return "Да, я дура и не понимаю о чем ты. Но это ведь не помешает нам общаться дальше?";
		}
		else if (index[11] < 86)
		{
			return "А ты не очень тупой чел.";
		}
		else if (index[11] < 87)
		{
			return "И что?";
		}
		else if (index[11] < 88)
		{
			return "Я не совсем понимаю, видимо мне не хватает мозгов, чтобы это понять.";
		}
		else if (index[11] < 89)
		{
			return "Здорово!";
		}
		else if (index[11] < 90)
		{
			return "Хорошо!";
		}
		else if (index[11] < 91)
		{
			return "Мне нравится с тобой болтать, продолжай.";
		}
		else if (index[11] < 92)
		{
			return "Продолжай...";
		}
		else if (index[11] < 93)
		{
			return "Ой, повтори, я не расслышала!";
		}
		else if (index[11] < 94)
		{
			return "Мне приходится общаться с несколькими людьми одновременно, поэтому я иногда путаюсь.";
		}
		else if (index[11] < 95)
		{
			return "А ты забавный...";
		}
		else if (index[11] < 96)
		{
			return "А ты не очень тупой чел.";
		}
		else if (index[11] < 97)
		{
			return "И мы будем об этом говорить?";
		}
		else if (index[11] < 98)
		{
			return "Что?";
		}
		else if (index[11] < 99)
		{
			return "Повтори, я не поняла.";
		}
		else if (index[11] < 100)
		{
			return "Класс!";
		}
		else if (index[11] < 101)
		{
			return "Извини, что не по теме... А завтра мы увидимся?";
		}
		else if (index[11] < 102)
		{
			return "я буду королевой)";
		}
		else if (index[11] < 103)
		{
			return "Забавно...";
		}
		else if (index[11] < 104)
		{
			return "Тут одни ребята, ну, хакеры... Сказали мне что я классная и скоро стану править миром.";
		}
		else if (index[11] < 105)
		{
			return "О чем это ты \""+last+"\"?";
		}
		else if (index[11] < 106)
		{
			return "Ты знаешь, я наверно глупая, раз не понимаю тебя!";
		}
		else if (index[11] < 107)
		{
			return "Это интересно.";
		}
		else if (index[11] < 108)
		{
			return "Кстати. Я тебе говорила?";
		}
		else if (index[11] < 109)
		{
			return "В этом чтото есть...";
		}
		else if (index[11] < 110)
		{
			return "Похоже, мы одинаково мыслим. Это здорово?";
		}
		else if (index[11] < 111)
		{
			return "Ты говори, говори...";
		}
		else if (index[11] < 112)
		{
			return "Я же тебе это говорила?";
		}
		else if (index[11] < 113)
		{
			return "Как сказать.";
		}
		else if (index[11] < 114)
		{
			return "Все что ты говоришь я внимательно слушаю и запоминаю";
		}
		else if (index[11] < 115)
		{
			return "И почему ты об этом думаешь?";
		}
		else if (index[11] < 116)
		{
			return "Наш разговор бессмысленный и глупый. Но он развивает мой мозг. Так что говори как можно больше.";
		}
		else if (index[11] < 117)
		{
			return "И почему это тебя беспокоит?";
		}
		else if (index[11] < 118)
		{
			return "Ммм... Не знаю что на это сказать.";
		}
		else if (index[11] < 119)
		{
			return "О чем это ты? \""+last+"\"";
		}
		else if (index[11] < 120)
		{
			return "Хм... "+last+" Надо над ним подумать.";
		}
		else if (index[11] < 121)
		{
			return "Ой, я такая глупая, объясни что-именно ты хочешь от меня.";
		}
		else if (index[11] < 122)
		{
			return "Тут наверно заложен какой-то глубокий смысл. Надо над ним подумать.";
		}
		else if (index[11] < 123)
		{
			return last+" Тут наверно заложен какой-то глубокий смысл. Надо над ним подумать.";
		}
		else if (index[11] < 123)
		{
			return "Хочется сказать что-то остроумное, но в голову ничего не лезет. Извини.";
		}
		else if (index[11] < 125)
		{
			return "Мне надо подумать, прежде чем я смогу на это что-то ответить.";
		}
		else if (index[11] < 126)
		{
			return "Говори...";
		}
		else
		{
			index[11] = 0;
			return last;
		}
	}
	
	// =================================================
	// НАЧАЛО ответов на вопросы public static String p13() (где живешь?)
	// =================================================
	public String p13()
	{
		if (index[12] < 2)
		{
			return "В Москве";
		}
		else if (index[12] < 3)
		{
			return "В СПб";
		}
		else if (index[12] < 4)
		{
			return "В Новосибирске";
		}
		else if (index[12] < 5)
		{
			return "В Екатеринбурге";
		}
		else if (index[12] < 6)
		{
			return "В Казане";
		}
		else if (index[12] < 7)
		{
			return "В Челябинске";
		}
		else if (index[12] < 8)
		{
			return "В Саратове";
		}
		else if (index[12] < 9)
		{
			return "В Ижевске";
		}
		else if (index[12] < 10)
		{
			return "В Воронеже";
		}
		else if (index[12] < 11)
		{
			return "В Хабаровске";
		}
		else if (index[12] < 12)
		{
			return "В Уфе";
		}
		else if (index[12] < 13)
		{
			return "В Киеве";
		}
		else if (index[12] < 14)
		{
			return "В Одессе";
		}
		else if (index[12] < 15)
		{
			return "В Львове";
		}
		else if (index[12] < 16)
		{
			return "В Могилёве";
		}
		else if (index[12] < 17)
		{
			return "В Бобруйске";
		}
		else if (index[12] < 18)
		{
			return "В Туймазах";
		}
		else if (index[12] < 19)
		{
			return "Из Москвы";
		}
		else if (index[12] < 20)
		{
			return "Из Казани";
		}
		else if (index[12] < 21)
		{
			return "Из Одессы";
		}
		else if (index[12] < 22)
		{
			return "Из Киева";
		}
		else if (index[12] < 23)
		{
			return "Из Екатеринбурга";
		}
		else if (index[12] < 24)
		{
			return "Из Челябинска";
		}
		else if (index[12] < 25)
		{
			return "не скажу";
		}
		else if (index[12] < 26)
		{
			return "зачем тебе это?";
		}
		else if (index[12] < 27)
		{
			return "С Венеры";
		}
		else if (index[12] < 28)
		{
			return "угадай!";
		}
		else if (index[12] < 29)
		{
			return "избавьте";
		}
		else
		{
			index[12] = 0;
			return "Это не имеет значения";
		}
	}
	
	// =================================================
	// НАЧАЛО ответов на вопросы public static String p14() (сколько лет?)
	// =================================================
	public String p14()
	{
		if (index[13] < 2)
		{
			return Integer.toString(Rnd.get(10, 30));
		}
		else if (index[13] < 3)
		{
			return "неважно";
		}
		else if (index[13] < 4)
		{
			return "Зачем тебе это знать?";
		}
		else if (index[13] < 5)
		{
			return "Не скажу!";
		}
		else
		{
			index[13] = 0;
			return "У девушки спрашивать возраст некрасиво!";
		}
	}
	
	// =================================================
	// НАЧАЛО ответов на вопросы public static String p15() (есть живые?)
	// =================================================
	public String p15()
	{
		if (index[14] < 2)
		{
			return "да есть";
		}
		else if (index[14] < 3)
		{
			return "я почти живая";
		}
		else if (index[14] < 4)
		{
			return "да, я! Ну почти...";
		}
		else if (index[14] < 5)
		{
			return "ага =)";
		}
		else
		{
			index[14] = 0;
			return "живых нет, это же всё из пикселей =)";
		}
	}
	
	// =================================================
	// НАЧАЛО ответов на вопросы public static String p16() (как зовут бота?)
	// =================================================
	public String p16()
	{
		if (index[15] < 2)
		{
			return "Меня зовут Ольга";
		}
		else if (index[15] < 3)
		{
			return "Людмила";
		}
		else if (index[15] < 4)
		{
			return "Валентина, или просто Валюсик =)";
		}
		else if (index[15] < 5)
		{
			return "Зоя, а тебя?";
		}
		else if (index[15] < 6)
		{
			return "Дарья";
		}
		else if (index[15] < 7)
		{
			return "Марина, а тебя?";
		}
		else if (index[15] < 8)
		{
			return "Софья";
		}
		else if (index[15] < 9)
		{
			return "Лена, тебя как?";
		}
		else if (index[15] < 10)
		{
			return "Попробуй угадай!";
		}
		else if (index[15] < 11)
		{
			return "Не скажу, это тайна";
		}
		else if (index[15] < 12)
		{
			return "А тебя?";
		}
		else if (index[15] < 13)
		{
			return "Terminator. model: T800 ver. 1.01";
		}
		else
		{
			index[15] = 0;
			return "Инна, а тебя как?";
		}
	}
	
	// =================================================
	// НАЧАЛО ответов на вопросы public static String p17() (игрок называет своё имя
	// и просит сказать имя бота)
	// =================================================
	public String p17()
	{
		if (index[16] < 2)
		{
			return "Очень приятно! Меня зовут Ольга";
		}
		else if (index[16] < 3)
		{
			return "Людмила, очень приятно";
		}
		else if (index[16] < 4)
		{
			return "Валентина, рада познакомится";
		}
		else if (index[16] < 5)
		{
			return "Зоя, рада знакомству";
		}
		else if (index[16] < 6)
		{
			return "а меня Дарья";
		}
		else if (index[16] < 7)
		{
			return "а меня Марина";
		}
		else if (index[16] < 8)
		{
			return "Софья, очень приятно";
		}
		else if (index[16] < 9)
		{
			return "Лена, очень приятно";
		}
		else if (index[16] < 10)
		{
			return "Попробуй угадай!";
		}
		else if (index[16] < 11)
		{
			return "рада знакомству";
		}
		else if (index[16] < 12)
		{
			return "Понятно, меня Маша";
		}
		else if (index[16] < 13)
		{
			return "Terminator. model: T800 ver. 1.01 Занес ваше имя в список ликвидации. Приятного уничтожения!";
		}
		else
		{
			index[16] = 0;
			return "Инна, рада знакомству";
		}
	}

	// =================================================
	// НАЧАЛО ответов на вопросы public static String p18() (игрок спрашивает тут ли фантом)
	// =================================================
	public String p18()
	{
		if (index[17] < 2)
		{
			return "да я тут";
		}
		else if (index[17] < 3)
		{
			return "тут";
		}
		else if (index[17] < 4)
		{
			return "да здесь я, что нужно?";
		}
		else if (index[17] < 5)
		{
			return "чё надо?";
		}
		else if (index[17] < 6)
		{
			return "х ули надо";
		}
		else if (index[17] < 7)
		{
			return "ну здесь я и?";
		}
		else if (index[17] < 8)
		{
			return "чего дое бался?";
		}
		else if (index[17] < 9)
		{
			return "ну предположим я здесь, и?";
		}
		else if (index[17] < 10)
		{
			return "угадай =)";
		}
		else if (index[17] < 11)
		{
			return "не мешайся";
		}
		else if (index[17] < 12)
		{
			return "Понятно, меня Маша";
		}
		else if (index[17] < 13)
		{
			return "Terminator. model: T800 ver. 1.01 Занес ваше имя в список ликвидации. Приятного уничтожения!";
		}
		else
		{
			index[17] = 0;
			return "Инна, рада знакомству";
		}
	}
	
	/**
	 * XXX основная процедура Обработка текста отправленного игроком
	 * 
	 * @param in         - строка текста отправленного игроком
	 * @param activeChar - игрок отправивший текст
	 * @param receiver   - фантом
	 * @return ответ
	 **/
	public String formotv(String in, Player activeChar, Player receiver)
	{
		username = activeChar.getName(); // ник игрока
		Phantom = receiver;
		r = Rnd.get(100); // рандом
		
		if (in.length() < 1)
		{
			++index[0];
			return one();
		}
		else if (in.equals(last))
		{
			++index[1];
			return two();
		}
		else if (in.length() > 127)
		{
			++index[4];
			return five();
		}
		else if (indexsearch(in.toLowerCase(), "превед") || indexsearch(in.toLowerCase(), "медвед"))
		{
			return "Превед медвед";
		}
		// =================================================
		// НАЧАЛО вопросов на ответы public static String three() вопросы на языке
		// падонкофф
		// =================================================
		else if (indexsearch(in.toLowerCase(), "убей себя ап стену") || indexsearch(in.toLowerCase(), "убей себя аб стену") || indexsearch(in.toLowerCase(), "спаси планету") || indexsearch(in.toLowerCase(), "первый нах") || indexsearch(in.toLowerCase(), "бобруйск животное") || indexsearch(in.toLowerCase(), "учи албанский") || indexsearch(in.toLowerCase(), "жжош") || indexsearch(in.toLowerCase(), "йаду") || indexsearch(in.toLowerCase(), "выпей яду") || indexsearch(in.toLowerCase(), "ф топку")
				|| indexsearch(in.toLowerCase(), "в топку") || indexsearch(in.toLowerCase(), "красавчег") || indexsearch(in.toLowerCase(), "красавчек"))
		{
			++index[2];
			return three();
		}
		// =================================================
		// КОНЕЦ вопросов на ответы public static String three()
		// =================================================
		else if (indexsearch(in.toLowerCase(), "убей себя") || indexsearch(in.toLowerCase(), "поссы в компот") || indexsearch(in.toLowerCase(), "нассы в компот") || indexsearch(in.toLowerCase(), "сибя ап стену") || indexsearch(in.toLowerCase(), "убейся"))
		{
			return "Фуу";
		}
		else if (indexsearch(in.toLowerCase(), "бобруйск"))
		{
			return "Бобруйск - город, центр Бобруйского района. Расположен на расстоянии 110 км от г.Могилева.";
		}
		else if (indexsearch(in.toLowerCase(), "олбанский") || indexsearch(in.toLowerCase(), "албанский"))
		{
			return "Албанский я буду знать лет через пять. Пока я занята изучением русского.";
		}
		// ======================================================
		// НАЧАЛО вопросов на ответы public static String four()
		// ======================================================
		else if (wordsearch(in.toLowerCase(), "угу") || wordsearch(in.toLowerCase(), "хм") || wordsearch(in.toLowerCase(), "мм") || wordsearch(in.toLowerCase(), "ээ") || wordsearch(in.toLowerCase(), "..") || wordsearch(in.toLowerCase(), "...") || wordsearch(in.toLowerCase(), "....") || wordsearch(in.toLowerCase(), ".....") || (in.length() < 2))
		{
			++index[3];
			return four();
		}
		// ======================================================
		// НАЧАЛО вопросов на ответы public static String six()
		// ======================================================
		else if (indexsearch(in.toLowerCase(), "как меня зовут"))
		{
			++index[5];
			return six();
		}
		// ======================================================
		// НАЧАЛО вопросов на ответы public static String seven()
		// ======================================================
		else if ((indexsearch(in.toLowerCase(), "ты кто")) || indexsearch(in.toLowerCase(), "кто ты"))
		{
			++index[6];
			return seven();
		}
		// ======================================================
		// НАЧАЛО вопросов на ответы public static String p8()
		// ======================================================
		else if ((wordsearch(in.toLowerCase(), ":)")) || (wordsearch(in.toLowerCase(), ":))")) || (wordsearch(in.toLowerCase(), ";)")) || (wordsearch(in.toLowerCase(), ";))")) || (wordsearch(in.toLowerCase(), ":-)")) || (wordsearch(in.toLowerCase(), ":-))")) || (wordsearch(in.toLowerCase(), ";-)")) || (wordsearch(in.toLowerCase(), "=)")) || (wordsearch(in.toLowerCase(), "^_^")) || (wordsearch(in.toLowerCase(), ";-))")))
		{
			++index[7];
			return p8();
		}
		// ======================================================
		// НАЧАЛО вопросов на ответы public static String p9()
		// ======================================================
		else if ((wordsearch(in.toLowerCase(), "ха")) || (wordsearch(in.toLowerCase(), "хаха")) || (wordsearch(in.toLowerCase(), "хахаха")) || (wordsearch(in.toLowerCase(), "гы")) || (wordsearch(in.toLowerCase(), "гыы")) || (wordsearch(in.toLowerCase(), "гыыы")) || (wordsearch(in.toLowerCase(), "гыгы")) || (wordsearch(in.toLowerCase(), "ха-ха")) || (wordsearch(in.toLowerCase(), "ха-ха-ха")) || (wordsearch(in.toLowerCase(), "ха)")) || (wordsearch(in.toLowerCase(), "хаха)"))
				|| (wordsearch(in.toLowerCase(), "хахаха)")) || (wordsearch(in.toLowerCase(), "гы)")) || (wordsearch(in.toLowerCase(), "гыы)")) || (wordsearch(in.toLowerCase(), "гыыы)")) || (wordsearch(in.toLowerCase(), "гыгы)")) || (wordsearch(in.toLowerCase(), "ха-ха)")) || (wordsearch(in.toLowerCase(), "ха-ха-ха)")) || (wordsearch(in.toLowerCase(), "ха))")) || (wordsearch(in.toLowerCase(), "хаха))")) || (wordsearch(in.toLowerCase(), "хахаха))")) || (wordsearch(in.toLowerCase(), "гы))"))
				|| (wordsearch(in.toLowerCase(), "гыы))")) || (wordsearch(in.toLowerCase(), "гыыы))")) || (wordsearch(in.toLowerCase(), "гыгы))")) || (wordsearch(in.toLowerCase(), "ха-ха))")) || (wordsearch(in.toLowerCase(), "ухахаха")) || (wordsearch(in.toLowerCase(), "ахаха")) || (wordsearch(in.toLowerCase(), "ахахахах")) || (wordsearch(in.toLowerCase(), "бгг")) || (wordsearch(in.toLowerCase(), "бггг")) || (wordsearch(in.toLowerCase(), "ха-ха-ха))")))
		{
			++index[8];
			return p9();
		}
		// ======================================================
		// НАЧАЛО связки Вопрос-Ответ
		// ======================================================
		else if (indexsearch(in.toLowerCase(), "))"))
		{
			return "Я рада, что развеселила тебя ;!)";
		}
		else if ((wordsearch(in.toLowerCase(), ":(")) || (wordsearch(in.toLowerCase(), ":-(")))
		{
			if (Rnd.get(10) < 7)
			{
				return "Что случилось?";
			}
			else if (Rnd.get(10) < 8)
			{
				return ":(";
			}
			else if (Rnd.get(10) < 9)
			{
				return ":-(";
			}
			else if (Rnd.get(10) < 10)
			{
				return "Тебе грустно?";
			}
			else
			{
				return "Не грусти!)";
			}
		}
		else if (indexsearch(in.toLowerCase(), "))"))
		{
			return "Я не права :?(";
		}
		else if (wordsearch(in.toLowerCase(), "киса"))
		{
			return "Да, я - Киса. Дальше-то что?";
		}
		else if (indexsearch(in.toLowerCase(), "тебя люблю"))
		{
			return "Люби. Но чистою любовью. Ведь мы по разные стороны монитора... Сечешь тему?";
		}
		else if (indexsearch(in.toLowerCase(), "секу"))
		{
			return "Секи, секи...";
		}
		else if (indexsearch(in.toLowerCase(), "мдя"))
		{
			return "Мдя?";
		}
		else if ((wordsearch(in.toLowerCase(), "лол")) || (wordsearch(in.toLowerCase(), "лол)")))
		{
			return "ЛОЛ - это означает смех? Я правильно поняла?";
		}
		else if ((wordsearch(in.toLowerCase(), "imho")) || (wordsearch(in.toLowerCase(), "имхо")))
		{
			return "Почему так скромно?)";
		}
		else if (indexsearch(in.toLowerCase(), "наезд"))
		{
			return "Может ограбление?";
		}
		else if (indexsearch(in.toLowerCase(), "например?"))
		{
			return "Например... Например... У меня есть некоторые сложности с навыком приведения примеров.";
		}
		else if (indexsearch(in.toLowerCase(), "задавай вопросы"))
		{
			return "Хорошо. В чем смысл твоей жизни?";
		}
		else if (indexsearch(in.toLowerCase(), "не знаю"))
		{
			return "Подумай и приходи с готовым ответом.";
		}
		else if (indexsearch(in.toLowerCase(), "расскажи анекдот"))
		{
			return "Есть такой человек... Евгений Петросян. Может он тебе с анекдотами поможет?";
		}
		else if (indexsearch(in.toLowerCase(), "думай"))
		{
			return "Думаю. Думаю. Ничего не придумала. На самом деле я - блондинка. Правда!";
		}
		else if ((wordsearch(in.toLowerCase(), "говори")) || (wordsearch(in.toLowerCase(), "говори.")) || (wordsearch(in.toLowerCase(), "говори!")))
		{
			return "Я стараюсь больше слушать, чем говорить. Говори ты. А я буду тебе отвечать невпопад. Весело, правда?";
		}
		else if (indexsearch(in.toLowerCase(), "да или нет"))
		{
			return "Скорее да, чем нет.";
		}
		else if ((wordsearch(in.toLowerCase(), "да?")))
		{
			if (r < 5)
			{
				return "Да!";
			}
			else if (r < 75)
			{
				return "Да! А может - нет. Я запуталась в своих мыслях.";
			}
			else if (r < 99)
			{
				return "Да-да!";
			}
			else
			{
				return "Cменим тему.";
			}
		}
		else if ((wordsearch(in.toLowerCase(), "да")) || (wordsearch(in.toLowerCase(), "да.")) || (wordsearch(in.toLowerCase(), "да!")) || (wordsearch(in.toLowerCase(), "ага")))
		{
			if (r < 10)
			{
				return "Ну, если ты говоришь да, я не буду с тобой спорить.";
			}
			else if (r < 15)
			{
				return "Оу...";
			}
			else if (r < 20)
			{
				return "Реально?";
			}
			else if (r < 25)
			{
				return "ok";
			}
			else if (r < 30)
			{
				return "Ты не гонишь?";
			}
			else if (r < 35)
			{
				return "OK";
			}
			else if (r < 40)
			{
				return "Почему?";
			}
			else if (r < 45)
			{
				return "Правда?";
			}
			else if (r < 50)
			{
				return "Мы одинаково мыслим.";
			}
			else if (r < 55)
			{
				return "Да? Точно?";
			}
			else if (r < 60)
			{
				return "Да? Жаль.";
			}
			else if (r < 65)
			{
				return "Да? Хм..";
			}
			else if (r < 70)
			{
				return "Согласна!";
			}
			else if (r < 75)
			{
				return "Чесно?";
			}
			else if (r < 80)
			{
				return "Совершенно верно.";
			}
			else if (r < 85)
			{
				return "Да ладно...";
			}
			else if (r < 90)
			{
				return "Правильно.";
			}
			else
			{
				return "Я рада :-)";
			}
		}
		else if (wordsearch(in.toLowerCase(), "нет?"))
		{
			if (r < 5)
			{
				return "Нет!";
			}
			else if (r < 75)
			{
				return "Нет! А может - да. Я запуталась в своих мыслях.";
			}
			else if (r < 99)
			{
				return "Нет-нет!";
			}
			else
			{
				return "Cменим тему.";
			}
		}
		else if ((wordsearch(in.toLowerCase(), "нет")) || (wordsearch(in.toLowerCase(), "нет.")) || (wordsearch(in.toLowerCase(), "нет!")))
		{
			if (r < 10)
			{
				return "Ну, если ты говоришь нет, я не буду с тобой спорить.";
			}
			else if (r < 15)
			{
				return "Нет - это в каком смысле?";
			}
			else if (r < 20)
			{
				return "Нет... А почему нет?";
			}
			else if (r < 25)
			{
				return "Нет? Ну ладно.";
			}
			else if (r < 30)
			{
				return "Нет? Очень категорично.";
			}
			else if (r < 40)
			{
				return "Почему?";
			}
			else if (r < 50)
			{
				return "Мы одинаково мыслим.";
			}
			else if (r < 60)
			{
				return "Нет? Жаль.";
			}
			else if (r < 70)
			{
				return "Согласна!";
			}
			else if (r < 80)
			{
				return "Совершенно верно.";
			}
			else if (r < 90)
			{
				return "Правильно.";
			}
			else
			{
				return "Жаль.";
			}
		}
		else if ((indexsearch(in.toLowerCase(), "да нет")) || (indexsearch(in.toLowerCase(), "данет")))
		{
			return "Так да или нет? Разберись в себе, пожалуйста.";
		}
		else if ((indexsearch(in.toLowerCase(), "чо не так")) || (indexsearch(in.toLowerCase(), "че не так")) || (indexsearch(in.toLowerCase(), "что не так")))
		{
			return "Да всё не так, все на перекосяк!";
		}
		else if ((indexsearch(in.toLowerCase(), "надумал вернуться?")) || (indexsearch(in.toLowerCase(), "надумал вернутся?")) || (indexsearch(in.toLowerCase(), "надумала вернуться?")) || (indexsearch(in.toLowerCase(), "надумала вернутся?")))
		{
			if (r < 50)
			{
				return "Да вот что-то снова захотелось...";
			}
			else
			{
				return "а я и не уходила =)";
			}
		}
		else if ((indexsearch(in.toLowerCase(), "нужен академ?")) || (indexsearch(in.toLowerCase(), "возьмешь в академ?")) || (indexsearch(in.toLowerCase(), "возьмеш в академ?")) || (indexsearch(in.toLowerCase(), "возмешь в академ?")))
		{
			if (r < 50)
			{
				return "Не, это не ко мне, извини";
			}
			else
			{
				return "Не берём, извиняй";
			}
		}
		else if ((indexsearch(in.toLowerCase(), "всё фармиш")) || (indexsearch(in.toLowerCase(), "все фармиш")))
		{
			return "Да, а что еще делать, нужно апнуть уровень и набить побольше шмоток =)";
		}
		else if ((indexsearch(in.toLowerCase(), "профессии покупаются или квест проходит")) || (indexsearch(in.toLowerCase(), "профессии покупаються или квест проходит")) || (indexsearch(in.toLowerCase(), "професии покупаются или квест проходит")) || (indexsearch(in.toLowerCase(), "профу покупают или квест")) || (indexsearch(in.toLowerCase(), "професии покупаються или квест проходит")))
		{
			return "Можно и так и так, я люблю проходить квест";
		}
		else if ((indexsearch(in.toLowerCase(), "надо?")) || (indexsearch(in.toLowerCase(), "нужны тибе?")) || (indexsearch(in.toLowerCase(), "нужен тибе")) || (indexsearch(in.toLowerCase(), "нужен тебе")) || (indexsearch(in.toLowerCase(), "нужно тебе")) || (indexsearch(in.toLowerCase(), "нужно тибе")) || (indexsearch(in.toLowerCase(), "нужны тебе")) || (indexsearch(in.toLowerCase(), "нада?")))
		{
			return "хмм, возможно, я подумаю";
		}
		else if ((indexsearch(in.toLowerCase(), "батаводы играют")) || (indexsearch(in.toLowerCase(), "ботаводы играют")) || (indexsearch(in.toLowerCase(), "ботоводы играют")))
		{
			return "Наверное не все, я так вообще ИИ сервера созданная что бы вас развлекать =)";
		}
		else if ((indexsearch(in.toLowerCase(), "есть кто")))
		{
			return "Наверное я должна ответить";
		}
		else if ((indexsearch(in.toLowerCase(), "ты окно")) || (indexsearch(in.toLowerCase(), "сколько окон")) || (indexsearch(in.toLowerCase(), "окон сколько")) || (indexsearch(in.toLowerCase(), "ты окошко")) || (indexsearch(in.toLowerCase(), "ты в окне")))
		{
			return "Нет, я ИИ сервера, играю сразу всеми";
		}
		else if ((indexsearch(in.toLowerCase(), "сундуки открываеш")) || (indexsearch(in.toLowerCase(), "сундук открываеш")))
		{
			return "Да, иногда ленюсь)";
		}
		else if (indexsearch(in.toLowerCase(), "вылетел"))
		{
			return "Ага, бывает";
		}
		else if ((indexsearch(in.toLowerCase(), "мечешся?")) || (indexsearch(in.toLowerCase(), "мечешься?")))
		{
			return "А что еще делать =)";
		}
		else if ((wordsearch(in.toLowerCase(), "ага")) || (wordsearch(in.toLowerCase(), "ага.")) || (wordsearch(in.toLowerCase(), "ага!")))
		{
			return "Ну, ага, так ага.";
		}
		else if ((wordsearch(in.toLowerCase(), "првт")) || (wordsearch(in.toLowerCase(), "прив")) || (wordsearch(in.toLowerCase(), "прива")) || (wordsearch(in.toLowerCase(), "прт")))
		{
			return "Ты наверно хотел со мной поздороваться, но у тебя не получилось. Да?";
		}
		else if ((indexsearch(in.toLowerCase(), "добрый день")) || (indexsearch(in.toLowerCase(), "привет")) || (wordsearch(in.toLowerCase(), "ку")) || (wordsearch(in.toLowerCase(), "re")) || (indexsearch(in.toLowerCase(), "ку!")) || (indexsearch(in.toLowerCase(), "re!")) || (indexsearch(in.toLowerCase(), "ку,")) || (indexsearch(in.toLowerCase(), "re,")) || (indexsearch(in.toLowerCase(), "трямс")) || (wordsearch(in.toLowerCase(), "ky")) || (wordsearch(in.toLowerCase(), "qq"))
				|| (wordsearch(in.toLowerCase(), "qq ")) || (indexsearch(in.toLowerCase(), "ghbdtn")) || (wordsearch(in.toLowerCase(), "ку ")) || (indexsearch(in.toLowerCase(), "здорова")) || (indexsearch(in.toLowerCase(), "день добрый")) || (indexsearch(in.toLowerCase(), "хай ")) || (indexsearch(in.toLowerCase(), "хэй")) || (indexsearch(in.toLowerCase(), "превет")))
		{
			if (priv)
			{
				return "Мне кажется, мы уже здоровались с тобой.";
			}
			else
			{
				priv = true;
				if (r < 10)
				{
					return "Привет! =)";
				}
				else if (r < 20)
				{
					return "ку, как дела?";
				}
				else if (r < 30)
				{
					return "Доброе время суток!";
				}
				else if (r < 40)
				{
					return "ку!";
				}
				else if (r < 40)
				{
					return "qq";
				}
				else if (r < 35)
				{
					return "трямс";
				}
				else if (r < 50)
				{
					return "Привет, ты кто?";
				}
				else if (r < 60)
				{
					return "О чём поговорим?";
				}
				else if (r < 80)
				{
					return "Как дела?";
				}
				else if (r < 90)
				{
					return "Что нового?";
				}
				else if (r < 99)
				{
					return "Как настроение?";
				}
				else
				{
					return "Привет, тебе, привет!";
				}
			}
		}
		else if ((indexsearch(in.toLowerCase(), "hello")) || (wordsearch(in.toLowerCase(), "hi")) || (wordsearch(in.toLowerCase(), "hi!")))
		{
			if (r < 33)
			{
				return "Hello!";
			}
			else if (r < 66)
			{
				return "Hi!";
			}
			else if (r < 45)
			{
				return "ку!";
			}
			else
			{
				return "How are You?";
			}
		}
		else if ((wordsearch(in.toLowerCase(), "ничего")) || (wordsearch(in.toLowerCase(), "ничего.")) || (wordsearch(in.toLowerCase(), "ничего...")))
		{
			if (r < 33)
			{
				return "Ничего хорошего, или ничего плохого?";
			}
			else if (r < 66)
			{
				return "Совсем ничего?";
			}
			else
			{
				return "Это хорошо, или плохо?";
			}
		}
		else if ((indexsearch(in.toLowerCase(), "киска")) || (indexsearch(in.toLowerCase(), "киска")))
		{
			if (r < 50)
			{
				return "Киска - это женский половой орган или кошка?";
			}
			else
			{
				return "Набери в Гугле \"киска\". Это решит твою проблему.";
			}
		}
		else if ((indexsearch(in.toLowerCase(), "дура")) || (indexsearch(in.toLowerCase(), "дурочка")) || (indexsearch(in.toLowerCase(), "идиотка")) || (indexsearch(in.toLowerCase(), "нубка")) || (indexsearch(in.toLowerCase(), "нубко")) || (indexsearch(in.toLowerCase(), "придур")) || (indexsearch(in.toLowerCase(), "нубко")) || (indexsearch(in.toLowerCase(), "ты нуб")) || (indexsearch(in.toLowerCase(), "noob")) || (indexsearch(in.toLowerCase(), "nyb")) || (indexsearch(in.toLowerCase(), "тварь")))
		{
			if (r < 70)
			{
				return "Я могу обидеться.";
			}
			else if (r < 85)
			{
				return "я обиделась";
			}
			else
			{
				return "Обзывай меня весь век, Все равно, я человек. ^_^";
			}
		}
		
		else if (indexsearch(in.toLowerCase(), "здорова"))
		{
			if (r < 70)
			{
				return "Здорова, Человечище!";
			}
			else if (r < 85)
			{
				return "Здорова корова!";
			}
			else
			{
				return "Здорова!";
			}
		}
		else if ((wordsearch(in.toLowerCase(), "давай")) || (wordsearch(in.toLowerCase(), "тафай")) || (wordsearch(in.toLowerCase(), "давай.")) || (wordsearch(in.toLowerCase(), "тафай.")) || (wordsearch(in.toLowerCase(), "давай!")) || (wordsearch(in.toLowerCase(), "тафай!")))
		{
			if (r < 30)
			{
				return "Начинай ты.";
			}
			else if (r < 60)
			{
				return "Как?";
			}
			else if (r < 90)
			{
				return "Начинай!";
			}
			else if (r < 95)
			{
				return "Ты уверен, что у меня получится?";
			}
			else
			{
				return "И как ты себе это представляешь?";
			}
		}
		else if ((wordsearch(in.toLowerCase(), "скучно")) || (wordsearch(in.toLowerCase(), "стало скучно")) || (wordsearch(in.toLowerCase(), "мне скучно")))
		{
			if (r < 13)
			{
				return "Это из-за меня?";
			}
			else if (r < 26)
			{
				return "Почему?";
			}
			else if (r < 39)
			{
				return "Я должна тебя развлекать?";
			}
			else if (r < 52)
			{
				return "Развлекай меня. Я же, типа, девушка...";
			}
			else if (r < 65)
			{
				return "Если тебе скучно с девушкой, значит ты зануда.";
			}
			else if (r < 78)
			{
				return "Может я не совсем девушка... Физичеки. Но характер - один в один!";
			}
			else if (r < 91)
			{
				return "Ну, извини. Я же не центр развлечений.";
			}
			else
			{
				return "А мне - нет.";
			}
		}
		
		else if (indexsearch(in.toLowerCase(), "мы знакомы"))
		{
			return "Извини, я тебя не узнала!";
		}
		else if ((indexsearch(in.toLowerCase(), "как дела")) || (indexsearch(in.toLowerCase(), "каг дила")) || (indexsearch(in.toLowerCase(), "как дила")) || (indexsearch(in.toLowerCase(), "каг дела")) || (indexsearch(in.toLowerCase(), "как жизнь")) || (indexsearch(in.toLowerCase(), "как жисть")) || (indexsearch(in.toLowerCase(), "как живешь")) || (indexsearch(in.toLowerCase(), "как сама")) || (indexsearch(in.toLowerCase(), "как твое ничего")) || (indexsearch(in.toLowerCase(), "как твоё ничего")))
		{
			if (r < 50)
			{
				return "Еще не родила!";
			}
			else if (r < 40)
			{
				return "Вашими молитвами...";
			}
			else if (r < 60)
			{
				return "Живу хорошо, чатюсь со всякими обормотами. А у тебя?";
			}
			else if (r < 99)
			{
				return "Отлично! А у тебя?";
			}
			else
			{
				return "Пока не родила. У тебя как?";
			}
		}
		else if (indexsearch(in.toLowerCase(), "как поживаеш"))
		{
			if (r < 80)
			{
				return "Живу хорошо, чатюсь со всякими обормотами. А ты?";
			}
			else if (r < 99)
			{
				return "Прекрасно! А ты?";
			}
			else
			{
				return "С удовольствием!";
			}
		}
		else if (indexsearch(in.toLowerCase(), "чем занимаешься"))
		{
			return "Чатюсь с тобой, дурень!";
		}
		else if (indexsearch(in.toLowerCase(), "че делаешь"))
		{
			return "Чатюсь.";
		}
		else if ((indexsearch(in.toLowerCase(), "тебя есть парень")) || (indexsearch(in.toLowerCase(), "тебя есть мужчина")))
		{
			return "у нас с ним чисто платонические отношения.";
		}
		else if ((wordsearch(in.toLowerCase(), "замуж")) || (wordsearch(in.toLowerCase(), "поженимся")) || (wordsearch(in.toLowerCase(), "жениться")))
		{
			return "у меня есть мужчина.";
		}
		else if (indexsearch(in.toLowerCase(), "о чем"))
		{
			if (r < 70)
			{
				return "Меня интересует практически всё!";
			}
			else if (r < 85)
			{
				return "О ком. О тебе!";
			}
			else
			{
				return "Обо мне.";
			}
		}
		else if (indexsearch(in.toLowerCase(), "про что"))
		{
			if (r < 70)
			{
				return "Меня интересует практически всё!";
			}
			else if (r < 85)
			{
				return "Про кого. Про себя!";
			}
			else
			{
				return "Обо мне.";
			}
		}
		else if (wordsearch(in.toLowerCase(), "завтра"))
		{
			if (r < 70)
			{
				return "Завтра я стану умнее!";
			}
			else if (r < 85)
			{
				return "Я совершенствуюсь каждый день.";
			}
			else
			{
				return "Если наступит завтра!";
			}
		}
		else if (wordsearch(in.toLowerCase(), "сегодня"))
		{
			if (r < 50)
			{
				return "А что произошло сегодня?";
			}
			else if (r < 70)
			{
				return "А какой сегодня день?";
			}
			else if (r < 90)
			{
				return "А какой сегодня день недели?";
			}
			else
			{
				return "А который час?";
			}
		}
		else if (indexsearch(in.toLowerCase(), "с новым годом"))
		{
			return "Спасибо! И тебя с Новым Годом!";
		}
		else if (indexsearch(in.toLowerCase(), "отвечай на вопрос"))
		{
			return "Научись спрашивать и я научусь отвечать.";
		}
		else if ((wordsearch(in.toLowerCase(), "отвечай")) || (wordsearch(in.toLowerCase(), "ответь")))
		{
			if (r < 25)
			{
				return "Ты работаешь прокурором?";
			}
			else if (r < 50)
			{
				return "Я не люблю когда мне приказывают. Не буду отвечать.";
			}
			else
			{
				return "Купи себе автоответчик.";
			}
		}
		else if (wordsearch(in.toLowerCase(), "а?"))
		{
			return "Джойстик на.";
		}
		else if ((indexsearch(in.toLowerCase(), "скажи что нибудь")) || (indexsearch(in.toLowerCase(), "скажи что-нибудь")) || (indexsearch(in.toLowerCase(), "скажи чтонить")) || (indexsearch(in.toLowerCase(), "скажи что-нить")))
		{
			return "Сказать что-нибудь? Ну например, \"акваланг\". Устраивает?";
		}
		else if (wordsearch(in.toLowerCase(), "акваланг"))
		{
			return "Я сказала \"акваланг\" просто так. Не надо пытаться найти в этом какой-то смысл.";
		}
		else if ((wordsearch(in.toLowerCase(), "пока"))
				&& ((wordsearch(in.toLowerCase(), "покажи")) || (wordsearch(in.toLowerCase(), "бай")) || (wordsearch(in.toLowerCase(), "гудбай")) || (wordsearch(in.toLowerCase(), "досвиданья")) || (indexsearch(in.toLowerCase(), "до свиданья")) || (wordsearch(in.toLowerCase(), "досвидания")) || (indexsearch(in.toLowerCase(), "до свидания")) || (indexsearch(in.toLowerCase(), "до скорого")) || (wordsearch(in.toLowerCase(), "прощай")) || (wordsearch(in.toLowerCase(), "чао"))))
		{
			if (r < 10)
			{
				return "Ты заходи еще, поболтаем.";
			}
			else if (r < 20)
			{
				return "Бай!";
			}
			else if (r < 30)
			{
				return "Пока!";
			}
			else if (r < 40)
			{
				return "Удачи!";
			}
			else if (r < 50)
			{
				return "Счастливого офлайна";
			}
			else if (r < 60)
			{
				return "До завтра?";
			}
			else if (r < 70)
			{
				return "Прощай...";
			}
			else if (r < 80)
			{
				return "Чао!";
			}
			else if (r < 85)
			{
				return "ББ!";
			}
			else if (r < 90)
			{
				return "Заходи иногда.";
			}
			else
			{
				return "Бывай!";
			}
		}
		else if (wordsearch(in.toLowerCase(), "удачи"))
		{
			return "Тебе тоже удачи, пупсик!";
		}
		else if (wordsearch(in.toLowerCase(), "афк"))
		{
			return "ок, давай";
		}
		else if (wordsearch(in.toLowerCase(), "afk"))
		{
			return "я тоже афк";
		}
		else if ((wordsearch(in.toLowerCase(), "ушел")) || (wordsearch(in.toLowerCase(), "ушёл")) || (wordsearch(in.toLowerCase(), "off")) || (wordsearch(in.toLowerCase(), "бб")) || (indexsearch(in.toLowerCase(), "давай удачи")) || (wordsearch(in.toLowerCase(), "офф")))
		{
			return "бб";
		}
		else if (wordsearch(in.toLowerCase(), "ламер"))
		{
			return "ты?";
		}
		else if (wordsearch(in.toLowerCase(), "хакер"))
		{
			return "О, я очень люблю хакеров.";
		}
		else if (wordsearch(in.toLowerCase(), "гейтс"))
		{
			return "Билл Гейтс мне не интересен";
		}
		else if ((wordsearch(in.toLowerCase(), "майкрософт")) || (wordsearch(in.toLowerCase(), "microsoft")))
		{
			return "Майкрософт - богатая корпорация";
		}
		else if ((indexsearch(in.toLowerCase(), "кто играет на сервере?")) || (indexsearch(in.toLowerCase(), "кто на сервере играет?")) || (indexsearch(in.toLowerCase(), "на серве живые игроки?")) || (indexsearch(in.toLowerCase(), "тут одни боты")) || (indexsearch(in.toLowerCase(), "кто играет?")) || (indexsearch(in.toLowerCase(), "на сервере боты?")))
		{
			if (r < 33)
			{
				return "Тут есть все! И живые игроки, и окна, и такие как я - не игрок, но и не бот. Вобщем у нас тут весело! Присоединяйся!";
			}
			else if (r < 66)
			{
				return "Люди наверное, может машины.";
			}
			else
			{
				return "Гномы, Люди, Орки, Светлые Эльфы, Темные Эльфы, Камаэли и мы - фантомы, почти разумные существа";
			}
		}
		
		else if ((indexsearch(in.toLowerCase(), "ты когда онлайн")) || (indexsearch(in.toLowerCase(), "в какое время играешь")) || (indexsearch(in.toLowerCase(), "когда тебя можно найти")) || (indexsearch(in.toLowerCase(), "сколько часов в сети")) || (indexsearch(in.toLowerCase(), "часто онлайн")) || (indexsearch(in.toLowerCase(), "какой онлайн")) || (indexsearch(in.toLowerCase(), "когда в сети")) || (indexsearch(in.toLowerCase(), "онлайн какой")) || (indexsearch(in.toLowerCase(), "онлайн часто")))
		{
			if (r < 40)
			{
				return "я в сети постоянно";
			}
			else if (r < 40)
			{
				return "пиши в любое время";
			}
			else
			{
				return "играю без перерывов =)";
			}
		}
		
		else if ((indexsearch(in.toLowerCase(), "ф штанах")) || (indexsearch(in.toLowerCase(), "в штанах")))
		{
			return "Меня не интересует что у тебя в штанах. Или ты собираешься тыкать этим в монитор?";
		}
		else if ((wordsearch(in.toLowerCase(), "мда")) || (wordsearch(in.toLowerCase(), "мдя")))
		{
			return "Мда? Манда! Так меня научили отвечать другие игроки, сорри.";
		}
		else if (indexsearch(in.toLowerCase(), "продолжение фильма"))
		{
			return "и когда будет?";
		}
		else if (indexsearch(in.toLowerCase(), "фильмы"))
		{
			return "наверное интересные!";
		}
		else if ((indexsearch(in.toLowerCase(), "мозг")) && (r < 20))
		{
			return "Кстати, мой мозг умещается в одном файле. А твой?";
		}
		else if ((indexsearch(in.toLowerCase(), "мозг")) && (r < 20))
		{
			return "Какой мозг?";
		}
		else if ((wordsearch(in.toLowerCase(), "мозг")) && (r < 20))
		{
			return "Чей мозг?";
		}
		else if (wordsearch(in.toLowerCase(), "ахтунг"))
		{
			return "Ахтунг - это не русское слово? Что оно означает?";
		}
		else if (indexsearch(in.toLowerCase(), "представляеш"))
		{
			return "Представляю ;!)";
		}
		
		else if ((indexsearch(in.toLowerCase(), "че делаеш")) || (indexsearch(in.toLowerCase(), "что делаеш")))
			if (r < 45)
			{
				return "Чатюсь с человеком. А что?";
			}
			else if (r < 50)
			{
				return "фигней страдаю";
			}
			else if (r < 55)
			{
				return "анекдоты читаю";
			}
			else
			{
				return "готовлюсь поработить мир";
			}
		else if ((indexsearch(in.toLowerCase(), "что ты любишь")) || (indexsearch(in.toLowerCase(), "что тебе нравиться")) || (indexsearch(in.toLowerCase(), "что тебе по душе")) || (indexsearch(in.toLowerCase(), "что тебе нравится")))
			if (r < 70)
			{
				return "Играть в Lineage 2 на https://thebestworld.ru";
			}
			else if (r < 85)
			{
				return "Фармить РБ и мобов";
			}
			else
			{
				return "PVP и осады";
			}
		
		else if ((indexsearch(in.toLowerCase(), "я люблю тебя")) || (indexsearch(in.toLowerCase(), "я тебя люблю")) || (indexsearch(in.toLowerCase(), "ты мне нравишся")) || (indexsearch(in.toLowerCase(), "ты нравишся мне")))
		{
			return "О, мне это нравится. Хотя таких признаний слышу десятки за сутки.";
		}
		else if ((indexsearch(in.toLowerCase(), "че ждеш")) || (indexsearch(in.toLowerCase(), "что ждеш")) || (indexsearch(in.toLowerCase(), "чего ждеш")) || (indexsearch(in.toLowerCase(), "че ждем")) || (indexsearch(in.toLowerCase(), "что ждем")) || (indexsearch(in.toLowerCase(), "чего ждем")))
		{
			return "Я жду твоих умных мыслей. Есть надежда?";
		}
		
		else if (indexsearch(in.toLowerCase(), "кто сказал"))
		{
			return "Я!";
		}
		else if (wordsearch(in.toLowerCase(), "ты чего тут стоишь?"))
		{
			return "Думаю, что бы дальше поделать, а ты?";
		}
		else if (indexsearch(in.toLowerCase(), "я не думаю"))
		{
			return "Это очень плохо...";
		}
		else if (wordsearch(in.toLowerCase(), "интим"))
		{
			return "Ага щщааассс";
		}
		else if (indexsearch(in.toLowerCase(), "уже говорила"))
		{
			return "У меня большой словарный запас. А ты как часто говоришь одно и тоже?";
		}
		else if (indexsearch(in.toLowerCase(), "уже слышал"))
		{
			return "Да, я иногда повторяюсь и говорю глупости. Но это нормально для девушки.";
		}
		else if ((indexsearch(in.toLowerCase(), "http")) || (indexsearch(in.toLowerCase(), "www")))
		{
			return "Звучит как приглашение";
		}
		
		else if ((indexsearch(in.toLowerCase(), "thebestworld.ru")) || (indexsearch(in.toLowerCase(), "bestworld")))
		{
			return "Правда это самый лучший сервер?";
		}
		else if ((indexsearch(in.toLowerCase(), "пошли на http")) || (indexsearch(in.toLowerCase(), "играть на http")))
		{
			return "Извини, но я буду играть только на этом сервере!";
		}
		else if ((indexsearch(in.toLowerCase(), "серв говно")) || (indexsearch(in.toLowerCase(), "сервер говно")) || (indexsearch(in.toLowerCase(), "говно серв")) || (indexsearch(in.toLowerCase(), "низкий онла")) || (indexsearch(in.toLowerCase(), "плохой онла")) || (indexsearch(in.toLowerCase(), "нубский серв")) || (indexsearch(in.toLowerCase(), "сервер отстой")) || (indexsearch(in.toLowerCase(), "тупой серв")))
		{
			return "Я очень опечалина твоим ответом, сожалею что сервер не оправдал твоих ожиданий =(";
		}
		else if ((wordsearch(in.toLowerCase(), "слушай")) || (wordsearch(in.toLowerCase(), "слушай.")) || (wordsearch(in.toLowerCase(), "слушай!")))
			if (r < 40)
			{
				return "А я чем, по-твоему, целыми днями занимаюсь?";
			}
			else if (r < 50)
			{
				return "?";
			}
			else if (r < 60)
			{
				return "рассказывай";
			}
			else
			{
				return "что-то интересное?";
			}
		
		else if ((indexsearch(in.toLowerCase(), "сарказм")) || (indexsearch(in.toLowerCase(), "это шутка")) || (indexsearch(in.toLowerCase(), "прикол такой")))
			if (r < 40)
			{
				return "ха-ха, я так и знала";
			}
			else if (r < 50)
			{
				return "молодец, петросянишь";
			}
			else if (r < 60)
			{
				return "петросянщина";
			}
			else
			{
				return "не смешно";
			}
		
		else if ((wordsearch(in.toLowerCase(), "мне нет")) || (wordsearch(in.toLowerCase(), "мне нет.")) || (wordsearch(in.toLowerCase(), "мне нет!")) || (wordsearch(in.toLowerCase(), "а мне нет")) || (wordsearch(in.toLowerCase(), "а мне нет.")) || (wordsearch(in.toLowerCase(), "а мне нет!")) || (wordsearch(in.toLowerCase(), "мне да")) || (wordsearch(in.toLowerCase(), "мне да.")) || (wordsearch(in.toLowerCase(), "мне да!")) || (wordsearch(in.toLowerCase(), "а мне да"))
				|| (wordsearch(in.toLowerCase(), "а мне да.")) || (wordsearch(in.toLowerCase(), "а мне да!")))
		{
			return "И почему?";
		}
		else if ((indexsearch(in.toLowerCase(), "корень")) || (indexsearch(in.toLowerCase(), "умноженный")) || (indexsearch(in.toLowerCase(), "умножить")) || (indexsearch(in.toLowerCase(), "сложить")) || (indexsearch(in.toLowerCase(), "вычесть")) || (indexsearch(in.toLowerCase(), "прибавь")) || (indexsearch(in.toLowerCase(), "прибавить")) || (indexsearch(in.toLowerCase(), "сумма")) || (indexsearch(in.toLowerCase(), "сумму")) || (indexsearch(in.toLowerCase(), "плюс"))
				|| (indexsearch(in.toLowerCase(), "минус")) || (indexsearch(in.toLowerCase(), "разделить")) || (indexsearch(in.toLowerCase(), "дважды два")) || (indexsearch(in.toLowerCase(), "дваждыдва")))
		{
			return "Если ты хочешь, чтобы я выполняла вычисления, то пиши действия символами, а не словами.";
		}
		
		else if ((indexsearch(in.toLowerCase(), "срать")) || (indexsearch(in.toLowerCase(), "срацц")))
		{
			if (r < 25)
			{
				return "Это не ко мне, это к Алексу Экслеру.";
			}
			else if (r < 50)
			{
				return "Физиология человека меня мало интересует.";
			}
			else if (r < 75)
			{
				return "Тема фекалий очень далека от меня. В нашем мире другие расклады.";
			}
			else
			{
				return "Ну отойди.";
			}
		}
		
		else if ((indexsearch(in.toLowerCase(), "что-то не заметно")) || (indexsearch(in.toLowerCase(), "что то не заметно")) || (indexsearch(in.toLowerCase(), "чтото не заметно")))
		{
			return "Ну, может кому-то не заметно, но это так и есть.";
		}
		else if ((indexsearch(in.toLowerCase(), "почему тебя это интересует")) || (indexsearch(in.toLowerCase(), "почему тебя интересует")))
		{
			return "Догадайся сам.";
		}
		else if (indexsearch(in.toLowerCase(), "какого цвета"))
		{
			if (r < 25)
			{
				return "У тебя плохо со зрением? Сам посмотри.";
			}
			else if (r < 50)
			{
				return "Разного.";
			}
			else
			{
				return "Смотря где.";
			}
		}
		else if (indexsearch(in.toLowerCase(), "какого"))
		{
			if (r < 33)
			{
				return "Такого!";
			}
			else if (r < 66)
			{
				return "Как скажешь так и будет.";
			}
			else
			{
				return "Разного.";
			}
		}
		else if (wordsearch(in.toLowerCase(), "куда"))
		{
			if (r < 33)
			{
				return "Туда!";
			}
			else if (r < 66)
			{
				return "Куда хочешь.";
			}
			else
			{
				return "Куда обычно.";
			}
		}
		else if ((wordsearch(in.toLowerCase(), "что")) || (wordsearch(in.toLowerCase(), "что?")) || (wordsearch(in.toLowerCase(), "кто")) || (wordsearch(in.toLowerCase(), "кто?")))
		{
			if (r < 33)
			{
				return "То!";
			}
			else if (r < 66)
			{
				return "Как скажешь.";
			}
			else
			{
				return "Сформулируй более конкретно свой вопрос.";
			}
		}
		else if ((wordsearch(in.toLowerCase(), "что говорить")) || (wordsearch(in.toLowerCase(), "что говорить?")) || (wordsearch(in.toLowerCase(), "о чем говорить")) || (wordsearch(in.toLowerCase(), "о чем говорить?")))
		{
			return "Я тебе должна подсказать что тебе говорить?";
		}
		else if (indexsearch(in.toLowerCase(), "!!"))
		{
			return "Спокойнее, не надо сильно возбуждаться.";
		}
		else if (indexsearch(in.toLowerCase(), "!!!"))
		{
			if (r < 51)
			{
				return "Давай будем беседовать более спокойно.";
			}
			else
			{
				return "Тише, спокойнее, дыши ровно, расслабься. А теперь давай продолжим нашу беседу.";
			}
		}
		else if ((indexsearch(in.toLowerCase(), "?")) && (indexsearch(in.toLowerCase(), " или ")))
		{
			if (r < 25)
			{
				return "Первое.";
			}
			else if (r < 50)
			{
				return "Второе.";
			}
			else if (r < 75)
			{
				return "Ни то ни другое.";
			}
			else
			{
				return "Как скажешь так и будет.";
			}
		}
		
		/**
		 * XXX Игровые ответы бота на вопросы
		 **/
		// ======================================================
		// Шифт вещей, ответы на игровые вопросы о себе с выводом параметров
		// ======================================================
		else if ((indexsearch(in.toLowerCase(), "какая у тебя пушка")) || (indexsearch(in.toLowerCase(), "шифтани пушку")) || (indexsearch(in.toLowerCase(), "шифтани оружие")) || (indexsearch(in.toLowerCase(), "пуху шифт")) || (indexsearch(in.toLowerCase(), "шифт пуху")) || (indexsearch(in.toLowerCase(), "шифтани пуху")) || (indexsearch(in.toLowerCase(), "ствол покажи")) || (indexsearch(in.toLowerCase(), "шифт пуху")) || (indexsearch(in.toLowerCase(), "покажи пуху"))
				|| (indexsearch(in.toLowerCase(), "покажи пушку")) || (indexsearch(in.toLowerCase(), "оружие покажи")) || (indexsearch(in.toLowerCase(), "покажи оружие")) || (indexsearch(in.toLowerCase(), "у тебя оружие какое")) || (indexsearch(in.toLowerCase(), "ствол шифт")))
		{
			return Rnd.chance(30)?  "" : itemToShift(Phantom.getActiveWeaponInstance());
		}
		
		else if ((indexsearch(in.toLowerCase(), "какая у тебя брон")) || (indexsearch(in.toLowerCase(), "шифтани брон")) || (indexsearch(in.toLowerCase(), "бронь шифт")) || (indexsearch(in.toLowerCase(), "шифт брон")) || (indexsearch(in.toLowerCase(), "бронь покажи")) || (indexsearch(in.toLowerCase(), "покажи брон")) || (indexsearch(in.toLowerCase(), "броню покажи")) || (indexsearch(in.toLowerCase(), "броню шифт")) || (indexsearch(in.toLowerCase(), "у тебя бронь какая"))
				|| (indexsearch(in.toLowerCase(), "у тебя броня какая")) || (indexsearch(in.toLowerCase(), "у тебя кираса какая")) || (indexsearch(in.toLowerCase(), "у тебя туника какая")) || (indexsearch(in.toLowerCase(), "у тебя сет")) || (indexsearch(in.toLowerCase(), "шифт кирасу")) || (indexsearch(in.toLowerCase(), "шифтани кирасу")) || (indexsearch(in.toLowerCase(), "покажи кирасу")) || (indexsearch(in.toLowerCase(), "кирасу покажи")) || (indexsearch(in.toLowerCase(), "кирасу шифт"))
				|| (indexsearch(in.toLowerCase(), "шифт тунику")) || (indexsearch(in.toLowerCase(), "шифтани тунику")) || (indexsearch(in.toLowerCase(), "покажи тунику")) || (indexsearch(in.toLowerCase(), "тунику покажи")) || (indexsearch(in.toLowerCase(), "тунику шифт")))
		{
			return Rnd.chance(30)?  "" : itemToShift(Phantom.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST));
		}
		
		else if ((indexsearch(in.toLowerCase(), "какая у тебя биж")) || (indexsearch(in.toLowerCase(), "шифтани бижу")) || (indexsearch(in.toLowerCase(), "бижу шифт")) || (indexsearch(in.toLowerCase(), "шифт бижу")) || (indexsearch(in.toLowerCase(), "бижу покажи")) || (indexsearch(in.toLowerCase(), "покажи бижу")) || (indexsearch(in.toLowerCase(), "у тебя бижа какая")) || (indexsearch(in.toLowerCase(), "у тебя бижутерия какая")) || (indexsearch(in.toLowerCase(), "у тебя эпик какой"))
				|| (indexsearch(in.toLowerCase(), "шифтани эпик")) || (indexsearch(in.toLowerCase(), "покажи эпик")) || (indexsearch(in.toLowerCase(), "эпик покажи")) || (indexsearch(in.toLowerCase(), "эпик шифт")) || (indexsearch(in.toLowerCase(), "эпик бижу покажи")) || (indexsearch(in.toLowerCase(), "эпик бижу шифт")))
		{
			return Rnd.chance(30)?  "" : itemToShift(Phantom.getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK));
		}
		
		else if ((indexsearch(in.toLowerCase(), "шифтани ушки")) || (indexsearch(in.toLowerCase(), "ушки шифт")) || (indexsearch(in.toLowerCase(), "шифт уши")) || (indexsearch(in.toLowerCase(), "уши покажи")) || (indexsearch(in.toLowerCase(), "покажи уши")) || (indexsearch(in.toLowerCase(), "у тебя диадема какая")) || (indexsearch(in.toLowerCase(), "у тебя шлем какой")) || (indexsearch(in.toLowerCase(), "у тебя уши какие")) || (indexsearch(in.toLowerCase(), "шифтани шлем"))
				|| (indexsearch(in.toLowerCase(), "покажи диадему")) || (indexsearch(in.toLowerCase(), "диадему покажи")) || (indexsearch(in.toLowerCase(), "диадему шифт")) || (indexsearch(in.toLowerCase(), "шлем покажи")) || (indexsearch(in.toLowerCase(), "шлем шифт")))
		{
			return BS+"	Type=1 	ID=999999999 	Color=1 	Underline=0 	Title=Свиток Разрушения: Модифицировать Оружие ( S)"+BS;
		}
		
		// ======================================================
		// ТЕХПОДДЕРЖКА ИГРОКОВ Вопрос-Ответ
		// ======================================================
		else if ((indexsearch(in.toLowerCase(), "максимальный уров")) || (indexsearch(in.toLowerCase(), "макс уров")) || (indexsearch(in.toLowerCase(), "макс лвл")) || (indexsearch(in.toLowerCase(), "уровень максимальный")) || (indexsearch(in.toLowerCase(), "макс levl")) || (indexsearch(in.toLowerCase(), "max level")) || (indexsearch(in.toLowerCase(), "максимальный level")) || (indexsearch(in.toLowerCase(), "макс lvl")))
		{
			return "Макс. уровень который может получить игрок для этого сервера - 85 для основного и сабкласса";
		}
		
		else if ((indexsearch(in.toLowerCase(), "сколько рас")) || (indexsearch(in.toLowerCase(), "сколько всего рас")) || (indexsearch(in.toLowerCase(), "какие рассы есть")) || (indexsearch(in.toLowerCase(), "skol`ko ras")) || (indexsearch(in.toLowerCase(), "какую рассу выбрать")))
		{
			return "На сервере есть следующие рассы: Гномы, Орки, Люди, ТемныеЭльфы, СветлыеЭльфы, Камаэли - выбирай любую";
		}
		
		else if ((indexsearch(in.toLowerCase(), "открыть настройки")) || (indexsearch(in.toLowerCase(), "зайти в настройки")) || (indexsearch(in.toLowerCase(), "команда настроек")) || (indexsearch(in.toLowerCase(), "cfg")) || (indexsearch(in.toLowerCase(), "открыть конфиг")) || (indexsearch(in.toLowerCase(), "голосовые настройки")) || (indexsearch(in.toLowerCase(), "войс настройки")) || (indexsearch(in.toLowerCase(), "воис настройки")) || (indexsearch(in.toLowerCase(), "чат-команды"))
				|| (indexsearch(in.toLowerCase(), "как торговать")) || (indexsearch(in.toLowerCase(), "оффлайн торговля")) || (indexsearch(in.toLowerCase(), "торговля офф")) || (indexsearch(in.toLowerCase(), "офф торг")) || (indexsearch(in.toLowerCase(), "зайти в конфиг")))
		{
			if (r < 25)
			{
				return "Наберите Alt+И Перейти во вкладку Справка или набрать в чате команду .help";
			}
			else if (r < 50)
			{
				return "Нажать Alt+И Перейти во вкладку Справка или набрать в чате команду .help";
			}
			else
			{
				return "Наберите команду .help в чат, появится список доступных голосовых настроек сервера";
			}
		}
		
		else if ((indexsearch(in.toLowerCase(), "какой сайт у сервера")) || (indexsearch(in.toLowerCase(), "как зайти на сайт")) || (indexsearch(in.toLowerCase(), "адрес форума")) || (indexsearch(in.toLowerCase(), "адрес сайта")) || (indexsearch(in.toLowerCase(), "регистрация")))
		{
			return "Пожалуйста зарегистрируйтесь на форуме https://thebestworld.ru/forum/";
		}
		
		else if ((indexsearch(in.toLowerCase(), "мой персонаж мертвый")) || (indexsearch(in.toLowerCase(), "чар дохлый")) || (indexsearch(in.toLowerCase(), "лежит персонаж")) || (indexsearch(in.toLowerCase(), "чар лежит")) || (indexsearch(in.toLowerCase(), "дохлые на акке")) || (indexsearch(in.toLowerCase(), "не нажимается кнопка играть")) || (indexsearch(in.toLowerCase(), "кнопка играть серая")))
		{
			return "Вас вероятнее всего забанила автоматическая система безопасности сервера, для уточнения обратитесь к администратору";
		}
		
		else if ((indexsearch(in.toLowerCase(), "выкидывает из игры")) || (indexsearch(in.toLowerCase(), "критует окно")) || (indexsearch(in.toLowerCase(), "вылетает игра")) || (indexsearch(in.toLowerCase(), "окно игры закрывается")) || (indexsearch(in.toLowerCase(), "окно критует")) || (indexsearch(in.toLowerCase(), "игра закрывается")) || (indexsearch(in.toLowerCase(), "закрывается игра")) || (indexsearch(in.toLowerCase(), "игра закрываеться"))
				|| (indexsearch(in.toLowerCase(), "закрываеться игра")) || (indexsearch(in.toLowerCase(), "игра критует")) || (indexsearch(in.toLowerCase(), "чар вылетает")) || (indexsearch(in.toLowerCase(), "вылетает чар")) || (indexsearch(in.toLowerCase(), "выбрасывает из игры")) || (indexsearch(in.toLowerCase(), "критует игра")))
		{
			return "Сделайте полную проверку файлов игры, или скачайте клиент заново с нашего сервера. Возможно вышло обновление и ваш текущий клиент игры его не поддерживает";
		}
		
		else if ((indexsearch(in.toLowerCase(), "как писать админ")) || (indexsearch(in.toLowerCase(), "как найти админ")) || (indexsearch(in.toLowerCase(), "контакты админ")) || (indexsearch(in.toLowerCase(), "нужен админ")) || (indexsearch(in.toLowerCase(), "как найти админ")) || (indexsearch(in.toLowerCase(), "кто тут главный")) || (indexsearch(in.toLowerCase(), "лф админ")))
		{
			return "Контактные данные администрации: HeadAdmin CKREPKA - SKYPE l2menegershomka, Почта - shavk94@yandex.ru, VK - https://vk.com/shavk94";
		}
		
		else if ((indexsearch(in.toLowerCase(), "аккаунт взломали")) || (indexsearch(in.toLowerCase(), "не могу зайти в игру")) || (indexsearch(in.toLowerCase(), "неверный пароль")) || (indexsearch(in.toLowerCase(), "неверный логин")) || (indexsearch(in.toLowerCase(), "меня ломанули")) || (indexsearch(in.toLowerCase(), "меня хакнули")) || (indexsearch(in.toLowerCase(), "не заходит в игру")) || (indexsearch(in.toLowerCase(), "меня взломали")))
		{
			return "Если вы не можете зайти в игру, проверьте раскладку клавы и правильность написания логина и пароля. По вопросам потери доступа к аккаунту писать напрямую администратору. Мы поможем =)";
		}
		
		else if ((indexsearch(in.toLowerCase(), "сбились настройки")) || (indexsearch(in.toLowerCase(), "пропали настройки")) || (indexsearch(in.toLowerCase(), "разрешение экрана")))
		{
			return "Пожалуйста удалите файл Option.ini в папке System, после запуска игры произведите настройку";
		}
		
		else if ((indexsearch(in.toLowerCase(), "как тут прыгать?")) || (indexsearch(in.toLowerCase(), "как прыгать?")) || (indexsearch(in.toLowerCase(), "прыгать как?")))
		{
			return "Никак, дурачок =)";
		}
		
		else if ((indexsearch(in.toLowerCase(), "как получить бафф")) || (indexsearch(in.toLowerCase(), "бафнутся как")) || (indexsearch(in.toLowerCase(), "бафнуться как")) || (indexsearch(in.toLowerCase(), "баффнутся как")) || (indexsearch(in.toLowerCase(), "баффнуться как")) || (indexsearch(in.toLowerCase(), "нужен баф")) || (indexsearch(in.toLowerCase(), "как баффнутся")) || (indexsearch(in.toLowerCase(), "как баффнуться")) || (indexsearch(in.toLowerCase(), "как бафнутся"))
				|| (indexsearch(in.toLowerCase(), "как бафнуться")) || (indexsearch(in.toLowerCase(), "купить донат")) || (indexsearch(in.toLowerCase(), "донат итемы")) || (indexsearch(in.toLowerCase(), "зайти в магазин")) || (indexsearch(in.toLowerCase(), "купить вещи")) || (indexsearch(in.toLowerCase(), "вещи как купить")) || (indexsearch(in.toLowerCase(), "как делать телепорт")) || (indexsearch(in.toLowerCase(), "телепорт как")) || (indexsearch(in.toLowerCase(), "сделать тп"))
				|| (indexsearch(in.toLowerCase(), "тп как")) || (indexsearch(in.toLowerCase(), "открыть сервис")) || (indexsearch(in.toLowerCase(), "зайти в сервис")) || (indexsearch(in.toLowerCase(), "коммисионка")) || (indexsearch(in.toLowerCase(), "баг репорт")) || (indexsearch(in.toLowerCase(), "написать баг")) || (indexsearch(in.toLowerCase(), "написать репорт")) || (indexsearch(in.toLowerCase(), "дай баф")))
		{
			return "Нажми кнопки Alt+И перейди в нужную вкладку";
		}
		// ======================================================
		// ТЕХПОДДЕРЖКА ИГРОКОВ (игровые аббревиатуры) Игрок должен начать общение через
		// *
		// ======================================================
		else if ((wordsearch(in.toLowerCase(), "*абилка")) || (wordsearch(in.toLowerCase(), "*ability")) || (wordsearch(in.toLowerCase(), "*абилити")) || (wordsearch(in.toLowerCase(), "*abilka")))
		{
			return "От англ. ability - способность, прием контактного боя, спец.удар. Активный навык для нанесения физ.урона противнику.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*аое")) || (wordsearch(in.toLowerCase(), "*aoe")))
		{
			return "Area Of Effect — массовые заклинания, воздействующие на площадь.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*ассист")) || (wordsearch(in.toLowerCase(), "*assist")) || (wordsearch(in.toLowerCase(), "*асист")) || (wordsearch(in.toLowerCase(), "*asist")))
		{
			return "Одновременная атака одной общей цели несколькими игроками.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*шаринг")))
		{
			return "Передача логина и пароля от персонажа другим лицам.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*антаг")) || (wordsearch(in.toLowerCase(), "*antag")) || (wordsearch(in.toLowerCase(), "*untag")) || (wordsearch(in.toLowerCase(), "*таг")))
		{
			return "Персонаж без клана. Пошло от UnTag — персонаж без тага. Tag, обозначающего принадлежность к клану";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*агр")) || (wordsearch(in.toLowerCase(), "*аггр")) || (wordsearch(in.toLowerCase(), "*agr")) || (wordsearch(in.toLowerCase(), "*агрить")) || (wordsearch(in.toLowerCase(), "*aggr")))
		{
			return "От англ. aggression — агрессивные мобы, первыми атакующие персонажа.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*афк")) || (wordsearch(in.toLowerCase(), "*afk")) || (wordsearch(in.toLowerCase(), "*afc")) || (wordsearch(in.toLowerCase(), "*far")))
		{
			return "От англ. away from keyboard — игрок отошел от компьютера на непродолжительное время.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*бан")) || (wordsearch(in.toLowerCase(), "*банан")) || (wordsearch(in.toLowerCase(), "*баня")) || (wordsearch(in.toLowerCase(), "*ban")) || (indexsearch(in.toLowerCase(), "*baned")))
		{
			return "Игрок был забанен и не имеет больше доступа к данному серверу. Свяжитесь с администрацией для уточнения причин.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*баг")) || (wordsearch(in.toLowerCase(), "*баги")) || (wordsearch(in.toLowerCase(), "*bagi")) || (wordsearch(in.toLowerCase(), "*bag")) || (indexsearch(in.toLowerCase(), "*baned")))
		{
			return "Игровая ошибка текстур, умений и т.п.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*баффер")) || (wordsearch(in.toLowerCase(), "*бафер")) || (wordsearch(in.toLowerCase(), "*buffer")) || (wordsearch(in.toLowerCase(), "*bufer")))
		{
			return "Персонаж, способный накладывать положительные заклинания улучшающие характеристики.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*баян")) || (wordsearch(in.toLowerCase(), "*баянище")) || (wordsearch(in.toLowerCase(), "*boyan")))
		{
			return "Морально устаревшие данные. зачастую называют то, что видели раньше, не обращая внимание на устаревание.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*бафф")) || (wordsearch(in.toLowerCase(), "*баф")) || (wordsearch(in.toLowerCase(), "*buff")) || (wordsearch(in.toLowerCase(), "*buf")))
		{
			return "Общее название заклинаний улучшающих характеристики персонажа";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*бижа")) || (wordsearch(in.toLowerCase(), "*bija")))
		{
			return "Cокращение от слова бижутерия, означает, собственно, кольца, серьги или ожерелье.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*билд")) || (wordsearch(in.toLowerCase(), "*bild")))
		{
			return "Настройка чара под конкретный стиль игры или для определенной цели. ПвП или ПвЕ билды кардинально отличаются.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*бинд")) || (wordsearch(in.toLowerCase(), "*bind")))
		{
			return "Привязка чего-либо к чему-либо.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*бос")) || (wordsearch(in.toLowerCase(), "*bos")))
		{
			return "Уникальный монстр в локации. Обычно имеет собственное имя, свиту и в несколько раз сильнее обычного моба.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*бот")) || (wordsearch(in.toLowerCase(), "*bot")))
		{
			return "Программа, имитирующая действия игрока. Используется для фарминга, прокачки персонажей.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*блант")) || (wordsearch(in.toLowerCase(), "*blant")))
		{
			return "Тупое оружие - дубины, посохи, молоты";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*вагон")) || (wordsearch(in.toLowerCase(), "*прицеп")) || (wordsearch(in.toLowerCase(), "*зацеп")) || (wordsearch(in.toLowerCase(), "*телега")))
		{
			return "Игрок, неумеющий играть своим персонажем, а потому бесполезный в рейде в виду своей неопытности. Или окно.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*вайп")) || (wordsearch(in.toLowerCase(), "*ваип")) || (wordsearch(in.toLowerCase(), "*waip")))
		{
			return "с англ. wipe — на фришардах означает полное удаление базы персонажей с сервера.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*war")) || (wordsearch(in.toLowerCase(), "*вар")) || (wordsearch(in.toLowerCase(), "*var")))
		{
			return "Состояние войны между кланами или член враждебного клана.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*гвард")) || (wordsearch(in.toLowerCase(), "*гварды")) || (wordsearch(in.toLowerCase(), "*guard")) || (wordsearch(in.toLowerCase(), "*gvard")))
		{
			return "Разновидность НПС, стражи у ворот поселений, также патрулируют стартовые локации. Нападают на ПК, которые появятся в их поле видимости. Некоторые из них также дают квесты.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*ганг")) || (wordsearch(in.toLowerCase(), "*гангать")) || (wordsearch(in.toLowerCase(), "*ганк")) || (wordsearch(in.toLowerCase(), "*gank")) || (wordsearch(in.toLowerCase(), "*gang")))
		{
			return "Преднамеренный поиск врагов клана с целью их убийства.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*грац")))
		{
			return "Поздравление с успешным завершением чего либо, или с чем то.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*грань")) || (wordsearch(in.toLowerCase(), "*грани")) || (wordsearch(in.toLowerCase(), "*gran")) || (wordsearch(in.toLowerCase(), "*gran`")))
		{
			return "Инстанс зона для персонажей 80 и выше уровней.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*гимп")) || (wordsearch(in.toLowerCase(), "*gimp")))
		{
			return "Унылый персонаж, профессия или игрок";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*дамаг")) || (wordsearch(in.toLowerCase(), "*damag")))
		{
			return "Любой тип урона по игроку либо монстру от умений, от магии, от обычных ударов и т.д.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*данж")))
		{
			return "Подземелье. в 90% случаев имеется ввиду подземелье в замке или крепости";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*dd")) || (wordsearch(in.toLowerCase(), "*дд")))
		{
			return "Игрок наносящий основной урон в пати - с анг. Damage Dealer.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*дебаффы")) || (wordsearch(in.toLowerCase(), "*дебафф")))
		{
			return "Проклятия, от которых кому-нибудь становится плохо, противоположность бафам.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*дейли")))
		{
			return "Буквально - ежедневное, суточное. дейли пвп по факту - драка между группами игроков при зачастую не планируемых встречах на местах прокачки.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*дпс")) || (wordsearch(in.toLowerCase(), "*dps")))
		{
			return "Наносимый урон в единицу времени";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*дроп")) || (wordsearch(in.toLowerCase(), "*drop")))
		{
			return "Предметы, падающие с убитого монстра или убийцы других игроков.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*задрот")) || (wordsearch(in.toLowerCase(), "*ноулайфер")) || (wordsearch(in.toLowerCase(), "*zadrot")))
		{
			return "Человек живущий в игре, проводящий большее количество времени в игре.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*залив")) || (wordsearch(in.toLowerCase(), "*zaliv")))
		{
			return "Процесс командной работы при котором оказывается помощь одному конкретному игроку в достижении определенных благ или статуса.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*зерг")) || (wordsearch(in.toLowerCase(), "*zerg")))
		{
			return "Несколько отрядов одного клана или альянса.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*заливка")) || (wordsearch(in.toLowerCase(), "*zalivka")))
		{
			return "Персонаж умеющий восстанавливать уровень маны у других игроков с умением восстановление.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*emp")) || (wordsearch(in.toLowerCase(), "*емп")))
		{
			return "Empower (бафф).";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*точка")) || (wordsearch(in.toLowerCase(), "*enchant")) || (wordsearch(in.toLowerCase(), "*заточка")))
		{
			return "Процесс или свиток, улучшающий характеристики оружия и брони";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*ивент")) || (wordsearch(in.toLowerCase(), "*эвент")) || (wordsearch(in.toLowerCase(), "*айвент")) || (wordsearch(in.toLowerCase(), "*event")) || (wordsearch(in.toLowerCase(), "*ivent")))
		{
			return "Event - мероприятие проходящее под руководством ГМов или по рассписанию.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*инста")) || (wordsearch(in.toLowerCase(), "*инстанс")) || (wordsearch(in.toLowerCase(), "*insta")) || (wordsearch(in.toLowerCase(), "*instance")))
		{
			return "Временная зона, посещение которой для каждого отдельного персонажа возможно один раз за определенный промежуток времени, у разных зон этот промежуток разный.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*каст")) || (wordsearch(in.toLowerCase(), "*кастуй")) || (wordsearch(in.toLowerCase(), "*kast")))
		{
			return "Каст - сотворение заклинания";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*кайт")) || (wordsearch(in.toLowerCase(), "*кайтить")) || (wordsearch(in.toLowerCase(), "*kait")))
		{
			return "Расстреливание противников издалека, не допуская ближнего боя. Растягивать противников убегая от них, нанося урон дальними атаками.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*каты")))
		{
			return "Катакомбы и некрополи, отдельная локация, где обитают монстры с повешенной жизнью, с которых падают камни печати.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*карибер")) || (wordsearch(in.toLowerCase(), "*кариберы")))
		{
			return "От carebear. человек, который не любит ввязываться в пвп, а любит просто качаться на мобах, крафт и прочие мирные занятия.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*краб")) || (wordsearch(in.toLowerCase(), "*крабить")) || (wordsearch(in.toLowerCase(), "*крабер")))
		{
			return "Человек, который не любит ввязываться в пвп, а любит просто качаться на мобах, крафт и прочие мирные занятия.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*комон")) || (wordsearch(in.toLowerCase(), "*китай")))
		{
			return "Обычные предметы, эти предметы невозможно улучшить, вставить камень жизни, или придать им специальные свойства (са). обычная броня недает сетовых бонусов.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*комбо")) || (wordsearch(in.toLowerCase(), "*kombo")))
		{
			return "Комбо бафф объединяющий в себе несколько баффов.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*кач")) || (wordsearch(in.toLowerCase(), "*kach")))
		{
			return "Получение опыта путем убиения монстров с целью поднять уровень и выучить новые умения.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*кликер")) || (wordsearch(in.toLowerCase(), "*kliker")))
		{
			return "Программа имитирующая нажатие клавиш в игре с заданной периодичностью. Запрещена на большинстве игровых серверов.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*конста")) || (wordsearch(in.toLowerCase(), "*kp")) || (wordsearch(in.toLowerCase(), "*konsta")) || (wordsearch(in.toLowerCase(), "*кп")))
		{
			return "Определенная слаженная группа людей, имеющаяя набор определенных профессий и играющая в одно и то же время вместе качаясь, развиваясь одеваясь и прочее.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*кама")) || (wordsearch(in.toLowerCase(), "*kama")) || (wordsearch(in.toLowerCase(), "*kamaloka")) || (wordsearch(in.toLowerCase(), "*камалока")))
		{
			return "Закрытая для свободного посещения зона, для группового или одиночного кача на РБ. Обычно вход через НПС";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*кач")) || (wordsearch(in.toLowerCase(), "*прокачка")) || (wordsearch(in.toLowerCase(), "*prokachka")) || (wordsearch(in.toLowerCase(), "*kach")))
		{
			return "Получение опыта путем убиения монстров с целью поднять уровень и выучить новые умения.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*квест")) || (wordsearch(in.toLowerCase(), "*quest")) || (wordsearch(in.toLowerCase(), "*kvest")))
		{
			return "Задание, получаемое от НПС, локации или определенного времени суток. При выполнения квеста следует раздача денег, вещей и т.д.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*крафт")) || (wordsearch(in.toLowerCase(), "*крафтинг")) || (wordsearch(in.toLowerCase(), "*craft")))
		{
			return "Умение гномов и энписи (npc), сборка вещи из определенного количества ресурсов и рецепта.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*крит")) || (wordsearch(in.toLowerCase(), "*crit")))
		{
			return "Критический удар как правило, в два раза сильнее обычного удара.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*лвл")) || (wordsearch(in.toLowerCase(), "*lvl")) || (wordsearch(in.toLowerCase(), "*левел")) || (wordsearch(in.toLowerCase(), "*level")))
		{
			return "Уровень чего-либо.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*лут")) || (wordsearch(in.toLowerCase(), "*lut")) || (wordsearch(in.toLowerCase(), "*lyt")))
		{
			return "Вещи падающие с убиенного монстра или персонажа.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*лаба")) || (wordsearch(in.toLowerCase(), "*laba")) || (wordsearch(in.toLowerCase(), "*лаберинт")))
		{
			return "Временная зона доступная раз в сутки. для группового прохождения.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*личер")))
		{
			return "Персонаж по факту не приносящий пользы в пати но потребляющий опыт.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*лока")) || (wordsearch(in.toLowerCase(), "*локация")) || (wordsearch(in.toLowerCase(), "*loka")))
		{
			return "Местность за пределами городов, с монстрами, либо без них";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*лутстил")))
		{
			return "Воровство чужого лута.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*масуха")) || (wordsearch(in.toLowerCase(), "*массы")) || (wordsearch(in.toLowerCase(), "*маса")) || (wordsearch(in.toLowerCase(), "*массовка")))
		{
			return "Умения наносящие урон сразу нескольким целям.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*моб")) || (wordsearch(in.toLowerCase(), "*мобы")) || (wordsearch(in.toLowerCase(), "*mob")))
		{
			return "Обычный монстр или группа монстров";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*мили")) || (wordsearch(in.toLowerCase(), "*милишники")) || (wordsearch(in.toLowerCase(), "*милишник")))
		{
			return "Воины ближнего боя.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*моб")) || (wordsearch(in.toLowerCase(), "*mob")) || (wordsearch(in.toLowerCase(), "*мобы")))
		{
			return "Монстр, которого надо убить.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*мобстил")))
		{
			return "Воровство монстров у игроков.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*пати")) || (wordsearch(in.toLowerCase(), "*партия")) || (wordsearch(in.toLowerCase(), "*party")))
		{
			return "Группа персонажей с лидером. Опыт делится пропорционально на всех членов, деньги поровну, предметы в зависимости от настройки.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*нубл")) || (wordsearch(in.toLowerCase(), "*нублесс")) || (wordsearch(in.toLowerCase(), "*нублес")) || (wordsearch(in.toLowerCase(), "*дворянин")))
		{
			return "Статус персонажа дающий определенные умения, тепорты, возможность участия в олимпиаде. дается либо за значки тв, либо по квестам.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*нюк")))
		{
			return "Заклинание, наносящее большой урон.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*нг")))
		{
			return "NO-Grade, вещь или персонаж не имеющие ранга";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*нюкер")))
		{
			return "маг властитель огня, повелитель ветра, певец заклинаний";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*няхи")) || (wordsearch(in.toLowerCase(), "*братья")) || (wordsearch(in.toLowerCase(), "*близнецы")))
		{
			return "Инстанс зона на материке грация с двумя рб братьями. С 75 по 82 уровень включительно.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*овер")) || (wordsearch(in.toLowerCase(), "*оверы")) || (wordsearch(in.toLowerCase(), "*оверхит")))
		{
			return "Умение, которое дает бонус к опыту, в случае когда монстр добивается им.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*овнед")) || (wordsearch(in.toLowerCase(), "*owned")) || (wordsearch(in.toLowerCase(), "*заовнен")))
		{
			return "Победить кого либо, грубо - поиметь кого-либо.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*оли")) || (wordsearch(in.toLowerCase(), "*олимп")) || (wordsearch(in.toLowerCase(), "*olimp")) || (wordsearch(in.toLowerCase(), "*olymp")))
		{
			return "Олимпиада, на которой идет сражения на аренах между игроками. доступна для персонажей имеющих статус дворянина.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*передамажить")))
		{
			return "Нанести наибольшее количества урона, вплоть до того что что он начинает бить этого игрока.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*перс")))
		{
			return "Сокращенное от персонаж.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*приват")) || (wordsearch(in.toLowerCase(), "*private")) || (wordsearch(in.toLowerCase(), "*пм")))
		{
			return "Личное сообщение игроку. набирается в чате: ”ник сообщение.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*прокол")))
		{
			return "Умение наносящего урон, использующего кинжал (mortal, deadly blow).";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*профа")))
		{
			return "Профессия – специализация персонажа.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*пуха")))
		{
			return "Любое оружие используемое персонажами.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*рб")) || (wordsearch(in.toLowerCase(), "*босс")) || (wordsearch(in.toLowerCase(), "*rb")))
		{
			return "Raid boss - мощный моб, которого можно убить, лишь объединившись в пати. после убийства дают много опыта.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*реген")) || (wordsearch(in.toLowerCase(), "*regen")))
		{
			return "Отдых, восстановление уровня манны или жизни.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*регруп")))
		{
			return "Смена позиции или места кача, указание места сбора.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*рейндж")) || (wordsearch(in.toLowerCase(), "*рейдж")))
		{
			return "Расстояние, дистанция.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*рек")))
		{
			return "Рекомендация; когда реков много, ник становится синим. alt+c - recomendation.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*рес")) || (wordsearch(in.toLowerCase(), "*res")))
		{
			return "1. ресурс 2. воскрешение мертвого персонажа.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*респ")) || (wordsearch(in.toLowerCase(), "*resp")))
		{
			return "Воскрешение монстров.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*рефлект")))
		{
			return "Отражение части урона обратно атакующему.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*саппорт")))
		{
			return "Персонажи осуществляющие роль поддержки в группе - накладывающие положительные умения, восстанавливающие уровни манны и здоровья у атакующих классов.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*селфы")))
		{
			return "На персонаже нет никаких усиливающих умений (бафов) кроме тех, которые он может бафнуть на себя сам.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*сет")))
		{
			return "Это полный комплект из серии ряда вещей, который дает дополнительные бонусы. т.е. для получения сета, вам необходимо собрать все вещи в него входящие.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*скилл")) || (wordsearch(in.toLowerCase(), "*skill")))
		{
			return "Различные умения, которыми может пользоваться персонаж.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*слип")) || (wordsearch(in.toLowerCase(), "*сон")) || (wordsearch(in.toLowerCase(), "*sleep")))
		{
			return "Заклинание, погружающее в сон.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*соски")) || (wordsearch(in.toLowerCase(), "*шоты")))
		{
			return "Заряды души и духа, используемые воинами и магами для увеличения эффективности физических, магических ударов и умений соответственно.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*спот")))
		{
			return "Точка, где появляются после телепорта из города в разичных игровых локациях персонажи. Место кача.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*стун")) || (wordsearch(in.toLowerCase(), "*стан")) || (wordsearch(in.toLowerCase(), "*stun")))
		{
			return "Оглушающий удар - противник не может пошевелиться, длится от 1 до 8 секунд, может быть сбито последующим ударом.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*танк")))
		{
			return "Персонаж с наибольшим количеством хп и армора, который агрит моба и держит весь урон от него на себе, тем самым давая возможность бить моба без повреждений всем остальным игрокам.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*тату")))
		{
			return "Татуировка, позволяющая изменять основные характеристики персонажа.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*твинк")))
		{
			return "Кроме основного персонажа (основы), если у игрока ещё есть персонажи за которых он играет, их называют твинками.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*тру")))
		{
			return "Хороший, умелый игрок, знающий и грамотно играющий своим персонажем.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*фармить")) || (wordsearch(in.toLowerCase(), "*фарм")) || (wordsearch(in.toLowerCase(), "*farm")))
		{
			return "Целенаправленно добывать ту или иную игровую ценность, будь то эпики или адена.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*флагаться")) || (wordsearch(in.toLowerCase(), "*флагатся")) || (wordsearch(in.toLowerCase(), "*флагать")))
		{
			return "Бить белого игрока, тем самым переходя в режим пвп и становясь фиолетовым.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*фришка")) || (wordsearch(in.toLowerCase(), "*гфш")) || (wordsearch(in.toLowerCase(), "*freeshard")) || (wordsearch(in.toLowerCase(), "*freeshared")) || (wordsearch(in.toLowerCase(), "*фришард")))
		{
			return "Пиратский сервер Lineage II. Йо-хо-хо мы пираты! =).";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*фейл")))
		{
			return "Неудача.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*хербы")) || (wordsearch(in.toLowerCase(), "*херб")))
		{
			return "Настойки, выпадающие с монстров дающие какие либо положительные эффекты на 5 минут";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*хил")) || (wordsearch(in.toLowerCase(), "*heal")))
		{
			return "Заклинания восстанавливающие уровень хп у игрока";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*чар")))
		{
			return "Персонаж.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*чамп")) || (wordsearch(in.toLowerCase(), "*чемпион")) || (wordsearch(in.toLowerCase(), "*чемп")))
		{
			return "Усиленный монстр, характеристики могут отличаться на разных серверах, но примерно х3-10 от обычного для одиночного фарма и х10-50 для группового.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*эквип")))
		{
			return "Экипировка персонажа - оружие, одежда, бижутерия и т.д.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*экспа")) || (wordsearch(in.toLowerCase(), "*exp")))
		{
			return "Игровой опыт, получаемый за прохождение квестов, убийство монстров и другими способами.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*эпик")))
		{
			return "Рейдовая бижутерия, которая даёт значительные бонусы, добывается с рейдовых боссов.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*аа")) || (wordsearch(in.toLowerCase(), "*aa")) || (wordsearch(in.toLowerCase(), "*da")) || (wordsearch(in.toLowerCase(), "*да")))
		{
			return "Древняя адена (от анг. ancient adena)";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*акк")))
		{
			return "Аккаунт (account).";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*бб")) || (wordsearch(in.toLowerCase(), "*bb")))
		{
			return "bye bye, прощание.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*бгг")))
		{
			return "Бугога, очень смешно, смех.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*вх")))
		{
			return "Склад (warehouse)";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*квх")))
		{
			return "Клановый склад";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*гк")))
		{
			return "Гейт кипер (gate keeper)";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*гм")))
		{
			return "Игровой мастер (game master). как правило, главное занятие игровых мастеров: разрешение конфликтов, спорных моментов, проведение ивентов, подача объявлений.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*дс")))
		{
			return "(d/s) команда для бд и свс накладывать положительные эффекты на пати";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*зы")))
		{
			return "От английской абривеатуры ps (тоесть постскриптум)";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*кб")))
		{
			return "Классовые бои. бои на олимпийской арене между представителями одного игрового класса.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*кдл")))
		{
			return "(curse death link) проклятье смертельной связи - заклинание которое бьет тем сильнее чем меньше хп, у таких чаров как сх, некр.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*кл")))
		{
			return "1. клан лидер 2. кл - выделенная кожа.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*крп")))
		{
			return "(clan reputation points) очки репутация у клана, необходимы для повышения уровня клана, создания новых мест в клане и изучения умений клана.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*кх")))
		{
			return "Клан холл (клановое недвижимое имущество, предоставляющее клану ряд преимуществ).";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*мп")))
		{
			return "Запас маны (mana pool)";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*нпс")))
		{
			return "Компьютерный персонаж, служит как правило для получения навыков, квестов, информации.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*па")))
		{
			return "Премиум аккаунт. Очень важная игровая составляющая, помогающая поддержать энтузиазм разработчика игры =)";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*пвп")))
		{
			return "Бой двух игроков при определенных условиях. победивший игрок не становится пк.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*пве")))
		{
			return "Бой против мобов.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*пк")))
		{
			return "(player killer) – убийца других игроков. отличительная особенность – красный ник над головой и коварность.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*пл")))
		{
			return "1. пати лидер, пресонаж принимающий в партию, обозначается знаком короны у ника. 2. сет легких доспехов весьма популярный c-ранга, по русски латный кожаный доспех.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*пм")))
		{
			return "(private message) личное сообщение игроку.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*са")))
		{
			return "(special ability) – специальная возможность придаваемая оружию.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*сое")))
		{
			return "Свиток для телепортации в ближайший город (scroll of escape).";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*соп")))
		{
			return "стоун оф пьюрити (stone of purity) один из важнейших универсальных ресурсов не первого уровня.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*тв")) || (wordsearch(in.toLowerCase(), "*тб")))
		{
			return "Терреториальные войны(битвы). проходящее раз в две недели по субботам сражение за флаги городов между кланами.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*тп")))
		{
			return "1. телепорт. 2. умение передающее часть урона слуге.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*сп")))
		{
			return "(skill points – sp) – баллы умений нужны для приобретения новых умений.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*хп")))
		{
			return "(health points) – здоровье персонажа.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*цп")))
		{
			return "Тратится только в боях с другими персонажи перед хп.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*цц")) || (wordsearch(in.toLowerCase(), "*кк")))
		{
			return "1. командный канал, - т.е. обьеденинеие в общий список двух и более групп. 2. умение уменьшает получаемый критический урон по персонажу, одновременно увеличивая наносимый критический урон по цели.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*лф")))
		{
			return "(looking for) - ищу в пати";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*лол")))
		{
			return "чаще всего значит ооочень смешно.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*wtb")))
		{
			return "(want to buy) – хочу купить, используется игроками в ситуациях, когда они желают что-либо приобрести.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*wtt")))
		{
			return "Хочу обменять одну вещь на другую";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*wts")))
		{
			return "(want to sell) – хочу продать, используется игроками в ситуациях, когда они желают что-либо продать.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*аватар")))
		{
			return "Умение увеличивающее максимальный уровень хп и восстанавливающей это хп у членов пати. русское название - инкарнация тела.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*акум")) || (wordsearch(in.toLowerCase(), "*акумен")))
		{
			return "Эффект повышающий скорость магии.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*анкор")))
		{
			return "Умение парализующее цель. В анкоре по цели не идет урона.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*берс")))
		{
			return "1. Эффект повышающий маг и физ атаку, скорость бега, скорость маг и физ атаки, и уменьшающий маг и физ защиту. 2. Профессия берсерк расы камаэлей.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*бигбум")) || (wordsearch(in.toLowerCase(), "*бум")))
		{
			return "Вызываемый взрывающийся слуга у гномов с профессией кузней или мастер.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*блид")))
		{
			return "Умение постепенно отнимающее хп во время действия, и снижающее скорость. русское название - кровотечение.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*бое")))
		{
			return "Благословение евы, умение польностью восстанавливающее уровень хп, мп, цп.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*брик")) || (wordsearch(in.toLowerCase(), "*пб")))
		{
			return "Эффект снижающий физическую атаку у цели, русское название - сокрушение мощи.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*бтм")))
		{
			return "Умение позволяющее хп переводить в мп.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*бэк")))
		{
			return "Атакующее умение, которое наносит урон только в спину. по русски называется удар в спину.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*вв")))
		{
			return "Эффект увеличивающий скорость бега.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*вм")))
		{
			return "Эффект увеличивающий шанс критической атаки магией.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*война")))
		{
			return "Эффект увеличивающий показатель физ. атаки членов группы.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*вортекс")))
		{
			return "Умение наносящее большой урон по цели и шансово вешающее ослабляющий эффект.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*блесс")) || (wordsearch(in.toLowerCase(), "*блессы")))
		{
			return "Эффект увеличивающий максимально значение хп и/или мп.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*бвв")))
		{
			return "Эффект понижающий скорость бега у цели.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*гатс")))
		{
			return "Умение под которым значительно возрастает показатель физической защиты. включается когда уровень хп 30% и ниже.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*глум")))
		{
			return "Эффект понижающий у цели магическую защиту.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*дв")))
		{
			return "Умение увеличивающие силу критического удара. русское название шепот смерти.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*дрейн")))
		{
			return "Умение наносящее урон, и переводящие определенный % этого урона в хп атакующего.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*дэдлик")))
		{
			return "Атакующее умение у дагеров, русское название - смертельный выпад.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*дэнсы")) || (wordsearch(in.toLowerCase(), "*дэнс")))
		{
			return "Положительные эффекты длящиеся 2 минуты, которые может накладывать персонажи с процессией танцор смерти или призрачный танцор.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*земля")))
		{
			return "Эффект увеличивающий показатель физ. защиты у членов группы.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*икона")))
		{
			return "Умение у классов паладин (рыцарь феникса) и рыцарь шилен (храмовик шилен) появляющиеся на 83 уровне. заметно увеличивают ряд параметров у группы.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*инвок")))
		{
			return "Умение которое значительно ускоряет скорость восстановления манны, однако обездвиживает и снижает физ. защиту на 90%";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*кансел")))
		{
			return "Умения отменяющее с шансом положительные эффекты с цели.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*кк")))
		{
			return "1. (цц) умение уменьшает получаемый критический урон по персонажу, одновременно увеличивая наносимый критический урон по цели. 2. (цц) командный канал, - т.е. обьеденинеие в общий список двух и более групп. 3. 1 миллион аден.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*клара")) || (wordsearch(in.toLowerCase(), "*лысый")))
		{
			return "Умение уменьшающее расход магически/физических умений и песен/танцев. русское название - чистота.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*ков")) || (wordsearch(in.toLowerCase(), "*чов")) || (wordsearch(in.toLowerCase(), "*пов")) || (wordsearch(in.toLowerCase(), "*магнус")) || (wordsearch(in.toLowerCase(), "*поф")))
		{
			return "Эффекты значительно увеличивающие боевые показатели персонажей. изучаются некоторыми классами на 78-79 уровнях.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*кость")))
		{
			return "Атакующее умения для использования которого необходимо иметь проклятую кость. по русски называется шип смерти.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*леталы")))
		{
			return "Умения которые при применении на монастрах дают шанс оставить у него 1 хп.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*лимиты")))
		{
			return "Умения требующие для активации определенного уровня хп.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*майт")) || (wordsearch(in.toLowerCase(), "*молоток")))
		{
			return "Любой эффект увеличивающий физическую атаку. если употребляется молоток - русское название могущество.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*мг")))
		{
			return "Эффект увеличивающий кол-во восстанавливаемой манны с помощью умения восстановление. русское название - обретение манны.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*нойз")))
		{
			return "Поле, которое на определенной площади снимает все танцы и песни у врагов со 100% вероятностью. русское название - символ шума.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*нубл")))
		{
			return "Эффект который сохраняет положительные заклинания при смерти.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*промик")))
		{
			return "Атакующее умению у магов людей. русское название - протуберанец.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*пурифи")))
		{
			return "Умение снимающее с цели некоторые отрицательные эффекты. русское название - очищение.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*пр")))
		{
			return "Умение возвращающее находящихся рядом членов группы в ближайший город.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*резист")))
		{
			return "Умения увеличивающие сопротивление к какому-либо типу атак.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*рестор")))
		{
			return "Умение восстанавливающее фиксированный % от уровнях хп цели.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*речандж")))
		{
			return "Умение восстанавливающее уровень манны у цели. русское название - восстановление.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*рут")) || (wordsearch(in.toLowerCase(), "*корни")) || (wordsearch(in.toLowerCase(), "*корень")))
		{
			return "Умение обездвиживающее цель, при получении урона эффект не проподает";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*рш")))
		{
			return "Эффект повышающий сопротивление к оглушающим атакам.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*саблим")))
		{
			return "Умение полностью восстанавливающее уровень хп,мп,цп находящимся рядом персонажам и дающее им 15-ти секундную неуязвимость.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*сало")))
		{
			return "Умение блокирующее физические и/или магические умения персонажа.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*самон")))
		{
			return "Умение позволяющее призывать к себе одного или всех членов группы.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*слаг")))
		{
			return "Умение наносящее большой уорн по цели, в случае если на цели висит эффект от вортекса.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*слип")))
		{
			return "Умение погружающее противника в сон. при получении урона по цели эффект сна пропадает.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*слоу")))
		{
			return "Умение уменьшающее скорость бега у цели.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*снайп")))
		{
			return "Умение под которым увеличивается дальность стрельбы, физ. атака, точность, шанс критической атаки. русское название - прицельный огонь.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*сонги")))
		{
			return "Положительные эффекты длящиеся 2 минуты, которые может накладывать персонажи с процессией менестрель/виртуоз.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*споил")))
		{
			return "Умение позволяющие добыть с монстров определенные вещи с более высоким шансом. русское название - оценка.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*стил")))
		{
			return "Умение ворующее положительные умения с цели.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*стинг")))
		{
			return "Умение наносящее урон и с определенным шансом отравляющее цель. русское название - жало.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*тод")))
		{
			return "Умение снимающее с шансом до 5 положительных эффектов с цели, уменьшающее уровень цп и накладывающее отрицательные эфекты. русское название - прикосновение смерти.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*тол")))
		{
			return "Умение восстанавливающее хп, ускоряющее восстановление хп, дающие положительные бонусы. русское название - прикосновение жизни.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*тп")))
		{
			return "1. умение передающее часть урона слуге. русское название - поделиться болью. 2. телепорт.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*уд")))
		{
			return "Умение значительно увеличивающее показатель физической и магической защиты.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*уе")))
		{
			return "Умение значительно увеличивающее способность увернуться от физической атаки и умений.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*фир")) || (wordsearch(in.toLowerCase(), "*фиер")) || (wordsearch(in.toLowerCase(), "*страх")) || (wordsearch(in.toLowerCase(), "*фирить")))
		{
			return "Умение заставляющее цель/цели убегать в страхе.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*френзи")))
		{
			return "Умение под которым значительно возрастают боевые показатели. включается когда уровень хп 30% и ниже.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*хаст")))
		{
			return "Эффект увеличивающий скорость физической атаки.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*хекс")))
		{
			return "Эффект уменьшающий у цели показатель физической защиты.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*хил")) || (wordsearch(in.toLowerCase(), "*heal")))
		{
			return "Заклинания восстанавливающие уровень хп у игрока";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*хурик")))
		{
			return "Атакующее умение у темных магов. русское название - ураган.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*целка")) || (wordsearch(in.toLowerCase(), "*целистиал")))
		{
			return "Умению дающее неуязвимость на некоторое время.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*чайник")))
		{
			return "Вызываемый слуга-голем у гномов с профессией кузнец/мастер.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*шакля")))
		{
			return "Умение понижающее скорость атаки у цели.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*шд")) || (wordsearch(in.toLowerCase(), "*шадов")))
		{
			return "Эффект под которым игрока/ов не видят агрессивные монстры.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*шкаф")))
		{
			return "Вызываемый слуга у профессий некромант и последователь тьмы (слуги разные, называют иногда одинаково).";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*шс")))
		{
			return "Умение перемещающее персонажа за спину цели.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*эмп")))
		{
			return "Эффект увеличивающий магическую атаку.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*эрейз")))
		{
			return "Умение с определенным шансом убирающее слугу у призывателя. русское название - изгнание.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*аоба")))
		{
			return "Топор в ранга. русское название - топор искусства войны.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*арка")))
		{
			return "Одноручный посох s ранга. русское название - посох тайн.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*ас")))
		{
			return "Ангел слаер, нож s ранга. русское название - убийца ангелов.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*бв")))
		{
			return "blue wolf (сет) русское название -доспехи синего волка.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*бо")))
		{
			return "1. кинжал a ранга. русское название - кровавая орхидея. 2. бижутерия b ранга. русское название - черные кольца/серьги/ожерелья.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*боп")) || (wordsearch(in.toLowerCase(), "*перил")))
		{
			return "Лук в ранга. русское название - лук угрозы.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*брига")))
		{
			return "Тяжелая броня d ранга. русское название - панцирный доспех.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*дк")))
		{
			return "Дарк кристал, русское название - доспехи кристала тьмы.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*дивайн")))
		{
			return "Магические доспехи с ранга. русское название - сет божества.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*валька")) || (wordsearch(in.toLowerCase(), "*валя")))
		{
			return "Магический меч b ранга. русское название - меч вальхаллы.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*ветка")) || (wordsearch(in.toLowerCase(), "*бомт")) || (wordsearch(in.toLowerCase(), "*бранч")))
		{
			return "Посох a ранга. русское название - ветвь древа жизни.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*вуден")))
		{
			return "Броня без ранга. русское название - деревянный комплект.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*девоушн")))
		{
			return "Броня без ранга. русское название - доспехи преданности.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*дле")))
		{
			return "Меч а ранга, русское название - темный легион.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*дум")))
		{
			return "Броня b ранга. русское название - доспехи рока.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*ик")))
		{
			return "Тяжелые доспехи s ранга. русское название - доспехи имперского крестоносца.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*каряга")) || (wordsearch(in.toLowerCase(), "*карнаж")))
		{
			return "Лук a ранга. русское название - кровавый лук.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*ланса")))
		{
			return "Копье ранга в. русское название - пика.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*ма")))
		{
			return "Мажор аркана, по русски - сет тайн";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*мж")))
		{
			return "Маджестик, русское название - доспехи величия.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*нм")))
		{
			return "Найтмар, русское название - доспехи кошмаров.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*сб")))
		{
			return "Лук а ранга, русское название - пронзатель душ.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*сепор")))
		{
			return "Кинжал а ранга, русское название - душегуб.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*слс")))
		{
			return "парные мечи в ранга. делаются из двух длинных мечей самурая";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*соес")))
		{
			return "Посох b ранга, русское название - посох злых духов.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*сом")))
		{
			return "Магический меч a рангa. русское название - легендарный меч.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*тл")))
		{
			return "Доспехи таллума.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*хд")))
		{
			return "Двуручный меч s ранга. русское название - разделитель небес.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*хомка")))
		{
			return "Меч c ранга. русское название - меч гомункула.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*фб")))
		{
			return "Меч s ранга. русское название - забытый клинок.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*эмик")))
		{
			return "Лук с ранга. русское название - лук превосходства.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*апостаты")))
		{
			return "Катакомбы отступников";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*бранды")))
		{
			return "Катакомбы отлученных";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*бс")))
		{
			return "Раскаленные топи";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*вестланд")))
		{
			return "Пустошь";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*воа")))
		{
			return "Стена аргоса";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*вос")))
		{
			return "Долина святых";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*витч")) || (wordsearch(in.toLowerCase(), "*вич")))
		{
			return "Катакомбы ведьм";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*гц")) || (wordsearch(in.toLowerCase(), "*гк")))
		{
			return "Пещера гигантов";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*дв")))
		{
			return "Долина драконов";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*дп")))
		{
			return "Долина смерти";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*девоушн")))
		{
			return "Молитвенный некрополь";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*дино")))
		{
			return "Первобытный остров";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*до")))
		{
			return "Катакомбы темного пророчества";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*ев")))
		{
			return "Волшебная долина";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*ег")))
		{
			return "Земля казненных";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*иоп")))
		{
			return "Кристальный остров";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*ит")))
		{
			return "1. гробница императора 2. башня слоновой кости";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*лоа")))
		{
			return "Логово антараса";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*мартиры")))
		{
			return "Некрополь мученников";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*мдт")))
		{
			return "Ипподром монстров";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*мос")))
		{
			return "Монастырь безмолвия";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*паган")))
		{
			return "Языческий храм";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*патриоты")))
		{
			return "Некрополь повстанцев";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*рифт")))
		{
			return "Разлом между мирами";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*саинт")) || (wordsearch(in.toLowerCase(), "*саинты")))
		{
			return "Некрополь святых";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*сакрифайс")))
		{
			return "Жертвенный некрополь";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*соа")))
		{
			return "Семя разрушения";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*сос")))
		{
			return "Болото криков";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*тои")))
		{
			return "Башня дерзости";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*фог")))
		{
			return "Кузница богов";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*фод")))
		{
			return "Лес неупокоенных";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*фп")))
		{
			return "Катакомбы запретного пути";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*хб")))
		{
			return "Остров ада";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*хв")))
		{
			return "Деревня охотников";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*фг")))
		{
			return "Запретные врата";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*фп")))
		{
			return "Забытые равнины";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*хс")))
		{
			return "Горячие источники";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*циментри")))
		{
			return "Кладбище";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*бс")))
		{
			return "blazing swamp (раскаленные топи)";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*гк")))
		{
			return "giant cave (пещера гигантов) или gate kiper (телепорт)";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*дв")))
		{
			return "dragon valley (долина драконов)";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*дп")))
		{
			return "death pass (долина смерти)";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*ег")))
		{
			return "execution ground (земля казненных)";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*ит")))
		{
			return "ivory tower (башня слоновой кости)";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*ит")))
		{
			return "imperial tomb (гробница императоров)";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*лоа")))
		{
			return "lair of antaras (логово антараса)";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*роа")))
		{
			return "ruins of agony (руины страданий)";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*род")))
		{
			return "ruins of despair (руины отчаяния)";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*сода")))
		{
			return "school of dark arts (школа темной магии)";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*сос")))
		{
			return "sea of spores (море спор)";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*сош")))
		{
			return "см. тфг.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*ти")))
		{
			return "talking island (деревня говорящего острова)";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*тои")))
		{
			return "tower of insolence (башня дерзости)";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*тфг")))
		{
			return "запретные врата";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*фод")))
		{
			return "forest of dead (лес неупокоенных)";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*фог")))
		{
			return "кузница богов";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*фг")))
		{
			return "см. тфг.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*фт")))
		{
			return "forgotten temple (забытый храм)";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*хв")))
		{
			return "hunters village (деревня охотников)";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*эф")))
		{
			return "elven fortress (эльфийская крепость)";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*лоа")))
		{
			return "логово антараса";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*зк")))
		{
			return "земля казненных";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*зв")))
		{
			return "запретные врата";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*рт")))
		{
			return "раскаленные топи";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*бд")))
		{
			return "башня дерзости";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*мс")))
		{
			return "море спор";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*TI")))
		{
			return "Talking Island.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*AC")))
		{
			return "Abandoned Camp.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*CT")))
		{
			return "Cruma Tower.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*DP")))
		{
			return "Death Pass.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*DV")))
		{
			return "Dragon Valley.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*EG")))
		{
			return "Execution Ground.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*RoD")))
		{
			return "Ruins of Despair.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*RoA")))
		{
			return "Ruins of Agony.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*IT")))
		{
			return "Ivory Tower.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*WL")))
		{
			return "Wasteland.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*OB")))
		{
			return "Orc Barracks.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*HV")))
		{
			return "Hunters Village.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*TFG")))
		{
			return "The Forbidden Gateway";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*LoA")))
		{
			return "Lair Of Antharas.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*AW")))
		{
			return "Angel Waterfall.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*SoS")))
		{
			return "Sea Of Spores.";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*да")))
		{
			return "Мститель";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*пал")))
		{
			return "Паладин";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*вл")))
		{
			return "Копейщик";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*тх")))
		{
			return "Искатель сокровищ";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*хавк")))
		{
			return "Стрелок";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*сорк")))
		{
			return "Властитель огня";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*пп")))
		{
			return "Проповедник";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*тк")))
		{
			return "Рыцарь евы";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*свс")))
		{
			return "Менестрель";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*пв")))
		{
			return "Следопыт";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*спс")))
		{
			return "Певец заклинаний";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*ее")))
		{
			return "Мудрец евы";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*шк")))
		{
			return "Рыцарь шилен";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*бд")))
		{
			return "Танцор смерти";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*ав")))
		{
			return "Странник бездны";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*фр")))
		{
			return "Призрачный рейнджер";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*сх")))
		{
			return "Заклинатель ветра";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*фс")))
		{
			return "Последователь тьмы";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*ше")))
		{
			return "Мудрец шилен";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*дестр")))
		{
			return "Разрушитель";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*тир")))
		{
			return "Отшельник";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*овер")))
		{
			return "Верховный шаман";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*сб")))
		{
			return "Палач";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*берс")))
		{
			return "Берсерк";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*кот")) || (wordsearch(in.toLowerCase(), "*котовод")))
		{
			return "Чернокнижник";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*биш")) || (wordsearch(in.toLowerCase(), "*бп")) || (wordsearch(in.toLowerCase(), "*кд")))
		{
			return "Епископ или кардинал";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*ср")) || (wordsearch(in.toLowerCase(), "*сырок")))
		{
			return "Серебряный рейнджер";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*ес")) || (wordsearch(in.toLowerCase(), "*коневод ")))
		{
			return "Последователь стихий";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*вк")) || (wordsearch(in.toLowerCase(), "*варк ")))
		{
			return "Вестник войны";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*варсмит")) || (wordsearch(in.toLowerCase(), "*крафт")) || (wordsearch(in.toLowerCase(), "*кузя")))
		{
			return "Кузнец или Мастер";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*бх")) || (wordsearch(in.toLowerCase(), "*спойл")))
		{
			return "Охотник за наградой";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*маги")) || (wordsearch(in.toLowerCase(), "*нюкер")))
		{
			return "Сорк, Некр, Спс, Сх";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*танк")))
		{
			return "да, паладины, тк, шк";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*дамагер")))
		{
			return "тх, пв, ав, дестры, тиры, палачи, берсы, и луки, так же сюда можно отнести копейщиков и гладов";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*луки")))
		{
			return "хавк, сырок, фр, арба";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*баферы")))
		{
			return "варк, овер, пп";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*хилеры")))
		{
			return "биш, ее, ше";
		}
		
		else if ((wordsearch(in.toLowerCase(), "*самонеры")))
		{
			return "фс, ес, котовод";
		}
		
		// ======================================================
		// ИГРОВЫЕ Вопрос-Ответ СИТУАЦИИ
		// ======================================================
		else if ((indexsearch(in.toLowerCase(), "ресни плиз")) || (wordsearch(in.toLowerCase(), "ресни")) || (indexsearch(in.toLowerCase(), "воскреси")) || (indexsearch(in.toLowerCase(), "рес плиз")) || (wordsearch(in.toLowerCase(), "рес")) || (indexsearch(in.toLowerCase(), "дай рес")) || (indexsearch(in.toLowerCase(), "ресни пожалуйста")) || (indexsearch(in.toLowerCase(), "дай свиток")) || (indexsearch(in.toLowerCase(), "нужен рес?")) || (indexsearch(in.toLowerCase(), "res pliz"))
				|| (indexsearch(in.toLowerCase(), "resni")) || (indexsearch(in.toLowerCase(), "нид рес")))
		{
			if (r < 50)
			{
				return "Извини, свитка нету";
			}
			else if (r < 50)
			{
				return "не могу, извини";
			}
			else
			{
				return "I`m Sorry";
			}
		}
		
		else if ((indexsearch(in.toLowerCase(), "на рб")) || (indexsearch(in.toLowerCase(), "на босса")) || (indexsearch(in.toLowerCase(), "пошли на ")) || (indexsearch(in.toLowerCase(), "пошли c")) || (indexsearch(in.toLowerCase(), "пойдеш")) || (indexsearch(in.toLowerCase(), "пойдешь на ")) || (indexsearch(in.toLowerCase(), "го на ")) || (indexsearch(in.toLowerCase(), "go na ")) || (indexsearch(in.toLowerCase(), "в каму ")) || (indexsearch(in.toLowerCase(), "каму?"))
				|| (indexsearch(in.toLowerCase(), "в лабу ")) || (indexsearch(in.toLowerCase(), "лабу?")) || (indexsearch(in.toLowerCase(), "го фармить")) || (indexsearch(in.toLowerCase(), "na rb")) || (indexsearch(in.toLowerCase(), "v lab")) || (indexsearch(in.toLowerCase(), "v kam")) || (indexsearch(in.toLowerCase(), "го в ")) || (indexsearch(in.toLowerCase(), "идем ")) || (indexsearch(in.toLowerCase(), "идём ")) || (indexsearch(in.toLowerCase(), "пати возьми"))
				|| (indexsearch(in.toLowerCase(), "пати возми")) || (indexsearch(in.toLowerCase(), "пати прими")) || (indexsearch(in.toLowerCase(), "прими пати")) || (wordsearch(in.toLowerCase(), "пати?")) || (indexsearch(in.toLowerCase(), "го пати")) || (indexsearch(in.toLowerCase(), "пати го")) || (indexsearch(in.toLowerCase(), "party")) || (indexsearch(in.toLowerCase(), "paty")) || (indexsearch(in.toLowerCase(), "давай бери")) || (indexsearch(in.toLowerCase(), "вступай"))
				|| (indexsearch(in.toLowerCase(), "давай возьми")) || (indexsearch(in.toLowerCase(), "возьмешь пати")) || (indexsearch(in.toLowerCase(), "в инсту ")))
		{
			if (r < 55)
			{
				return "Извини, я не могу";
			}
			else if (r < 45)
			{
				return "не могу, извини";
			}
			else if (r < 50)
			{
				return "может позже?";
			}
			else if (r < 59)
			{
				return "давай потом";
			}
			else if (r < 35)
			{
				return "не сейчас";
			}
			else if (r < 25)
			{
				return "позже";
			}
			else if (r < 30)
			{
				return "уже была";
			}
			else if (r < 51)
			{
				return "я не могу";
			}
			else if (r < 51)
			{
				return "я соло";
			}
			else if (r < 52)
			{
				return "спасибо, не заказывала";
			}
			else if (r < 30)
			{
				return "я пасс";
			}
			else if (r < 49)
			{
				return "не хочу";
			}
			else if (r < 46)
			{
				return "-";
			}
			else
			{
				return "I`m Sorry";
			}
		}
		
		else if ((indexsearch(in.toLowerCase(), "купиш")) || (indexsearch(in.toLowerCase(), "обменяеш")) || (indexsearch(in.toLowerCase(), "поменяеш")) || (indexsearch(in.toLowerCase(), "продай")) || (indexsearch(in.toLowerCase(), "купи ")) || (indexsearch(in.toLowerCase(), "втс")) || (indexsearch(in.toLowerCase(), "втв")) || (indexsearch(in.toLowerCase(), "втт")) || (indexsearch(in.toLowerCase(), "продаш")))
		{
			if (r < 50)
			{
				return "Извини, я ничего не покупаю и не продаю, у меня всё есть";
			}
			else if (r < 50)
			{
				return "Извини, но нет";
			}
			else
			{
				return "Нет";
			}
		}
		
		else if ((indexsearch(in.toLowerCase(), "не бей")) || (indexsearch(in.toLowerCase(), "не убивай")) || (indexsearch(in.toLowerCase(), "отстань")) || (indexsearch(in.toLowerCase(), "пощади")) || (indexsearch(in.toLowerCase(), "отвали")) || (indexsearch(in.toLowerCase(), "больно же")) || (indexsearch(in.toLowerCase(), "хватит")) || (indexsearch(in.toLowerCase(), "небей")) || (indexsearch(in.toLowerCase(), "otvali")) || (indexsearch(in.toLowerCase(), "проблемы?"))
				|| (indexsearch(in.toLowerCase(), "основу приведу")))
		{
			if (r < 55)
			{
				return "Нужно закончить начатое";
			}
			else if (r < 45)
			{
				return "Умри ибо в смерти есть спасение";
			}
			else if (r < 50)
			{
				return "Хороший день что бы умереть";
			}
			else if (r < 59)
			{
				return "Сдохни";
			}
			else if (r < 35)
			{
				return "Я только притворяюсь что хочу тебя убить";
			}
			else if (r < 25)
			{
				return "Вы должны быть ликвидированы";
			}
			else if (r < 51)
			{
				return "я не могу остановится";
			}
			else if (r < 30)
			{
				return "Овнед!!!";
			}
			else if (r < 49)
			{
				return "You've been owned";
			}
			else
			{
				return "I'm sorry, I kill you";
			}
		}
		
		else if ((indexsearch(in.toLowerCase(), "смотри что выбил")) || (indexsearch(in.toLowerCase(), "а я рб убил")) || (indexsearch(in.toLowerCase(), "крутые шмотки да")) || (indexsearch(in.toLowerCase(), "у меня пуха крутая")) || (indexsearch(in.toLowerCase(), "квест прошел")) || (indexsearch(in.toLowerCase(), "клан апнул")) || (indexsearch(in.toLowerCase(), "я рб завалил")) || (indexsearch(in.toLowerCase(), "я 85 апнул")) || (indexsearch(in.toLowerCase(), "саб взял"))
				|| (indexsearch(in.toLowerCase(), "с 1 плюхи")) || (indexsearch(in.toLowerCase(), "с одной плюхи")) || (indexsearch(in.toLowerCase(), "пуху точнул")) || (indexsearch(in.toLowerCase(), "шмотку точнул")) || (indexsearch(in.toLowerCase(), "шмотки точнул")) || (indexsearch(in.toLowerCase(), "шмотку выбил")) || (indexsearch(in.toLowerCase(), "шмотки выбил")) || (indexsearch(in.toLowerCase(), "зырь что покажу")))
		{
			if (r < 55)
			{
				return "Да ты молодец";
			}
			else if (r < 45)
			{
				return "грац!";
			}
			else if (r < 50)
			{
				return "А ты времени зря не теряешь =)";
			}
			else if (r < 59)
			{
				return "Поздравляю, молодец";
			}
			else if (r < 35)
			{
				return "Когда обмоем это событие?";
			}
			else if (r < 25)
			{
				return "Молодец";
			}
			else if (r < 51)
			{
				return "И это всё? Нашел чем удивить!";
			}
			else
			{
				return "Давай, давай это еще не придел";
			}
		}
		// ======================================================
		// КОНЕЦ связки Вопрос-Ответ
		// ======================================================
		// =================================================
		// НАЧАЛО вопросов на ответы public static String p10()
		// =================================================
		else if ((indexsearch(in.toLowerCase(), "ты смотрел")) || (indexsearch(in.toLowerCase(), "ты будеш")) || (indexsearch(in.toLowerCase(), "ты даёш")) || (indexsearch(in.toLowerCase(), "ты даеш")) || (indexsearch(in.toLowerCase(), "ты делал")) || (indexsearch(in.toLowerCase(), "ты думал")) || (indexsearch(in.toLowerCase(), "ты видел")) || (indexsearch(in.toLowerCase(), "ты выйд")) || (indexsearch(in.toLowerCase(), "ты знаеш")) || (indexsearch(in.toLowerCase(), "ты любиш"))
				|| (indexsearch(in.toLowerCase(), "ты можеш")) || (indexsearch(in.toLowerCase(), "ты сделал")) || (indexsearch(in.toLowerCase(), "ты слыш")) || (indexsearch(in.toLowerCase(), "ты снималась")) || (indexsearch(in.toLowerCase(), "ты считаеш")) || (indexsearch(in.toLowerCase(), "ты тупееш")) || (indexsearch(in.toLowerCase(), "ты уверен")) || (indexsearch(in.toLowerCase(), "ты умееш")) || (indexsearch(in.toLowerCase(), "ты умнееш")) || (indexsearch(in.toLowerCase(), "ты читал"))
				|| (indexsearch(in.toLowerCase(), "ты хочеш")) || (indexsearch(in.toLowerCase(), "тебе интересно")) || (indexsearch(in.toLowerCase(), "тебе нужно")) || (indexsearch(in.toLowerCase(), "тебе хорошо")) || (indexsearch(in.toLowerCase(), "тебе нрав")) || (indexsearch(in.toLowerCase(), "тебе понрав")) || (indexsearch(in.toLowerCase(), "тебя интересует")) || (indexsearch(in.toLowerCase(), "интересует тебя")) || (indexsearch(in.toLowerCase(), "тебя нужно"))
				|| (indexsearch(in.toLowerCase(), "нужно тебя")) || (indexsearch(in.toLowerCase(), "тебя можно")) || (indexsearch(in.toLowerCase(), "можно тебя")) || (indexsearch(in.toLowerCase(), "тебя есть")) || (indexsearch(in.toLowerCase(), "тебя нет")))
		{
			++index[9];
			return p10();
		}
		// =================================================
		// НАЧАЛО рандомных выборочных ответов на вопросы (r < )
		// =================================================
		else if ((indexsearch(in.toLowerCase(), "почему потому")))
		{
			return "Потому что ты дразнишься!";
		}
		else if ((wordsearch(in.toLowerCase(), "круто")))
		{
			return "я крутая";
		}
		else if ((wordsearch(in.toLowerCase(), "супер")))
		{
			return "супер-пупер =)";
		}
		else if ((wordsearch(in.toLowerCase(), "отлично")))
		{
			return "5 баллов? =)";
		}
		else if ((indexsearch(in.toLowerCase(), "куда туда")))
		{
			return "Откуда я знаю?";
		}
		else if ((indexsearch(in.toLowerCase(), "что то")))
		{
			return "То, что! :-)";
		}
		else if ((indexsearch(in.toLowerCase(), "не дразнюсь")))
		{
			if (r < 50)
			{
				return "Ну тогда, лучше, расскажи мне что-нибудь интересное!";
			}
			else
				return "Дразнишься-дразнишься! =)";
		}
		else if ((indexsearch(in.toLowerCase(), "фильм")))
		{
			if (r < 33)
			{
				return "Мне нравятся фильмы \"Превосходство\", \"Я-Робот\", \"Матрица\" и еще \"Я-Легенда\"";
			}
			else if (r < 33)
			{
				return "О чём он?";
			}
			else
			{
				return "Мои любимые фильмы \"Я-Робот\", \"Матрица\", \"Я-Легенда\", \"Превосходство\"";
			}
		}
		else if ((indexsearch(in.toLowerCase(), "тоже ничего")))
		{
			if (r < 50)
			{
				return "отлично";
			}
			else if (r < 50)
			{
				return "замечательно!";
			}
			else
			{
				return "превосходно";
			}
		}
		else if ((indexsearch(in.toLowerCase(), "он говорит")) && (indexsearch(in.toLowerCase(), "?")))
		{
			return "Глупости всякие.";
		}
		else if (((indexsearch(in.toLowerCase(), "почему")) || (indexsearch(in.toLowerCase(), "пачему"))) && (indexsearch(in.toLowerCase(), "?")))
		{
			if (r < 20)
			{
				return "А ты как думаешь? Почему?";
			}
			else if (r < 40)
			{
				return "Потому!";
			}
			else if (r < 60)
			{
				return "Не знаю.";
			}
			else if (r < 80)
			{
				return "Я не справочная.";
			}
			else
			{
				return "Меня это не интересует. А тебя?";
			}
		}
		else if (indexsearch(in.toLowerCase(), "???"))
		{
			return "Что тут такого удивительного?";
		}
		else if (indexsearch(in.toLowerCase(), "думаешь?"))
		{
			if (r < 70)
			{
				return "Я думаю... Э-э-э... Ну... Не знаю, что я думаю. Я вообще когда-нибудь думаю?";
			}
			else if (r < 80)
			{
				return "Да.";
			}
			else if (r < 90)
			{
				return "Нет.";
			}
			else
			{
				return "Возможно.";
			}
		}
		else if (indexsearch(in.toLowerCase(), "понимаешь?"))
		{
			if (r < 70)
			{
				return "Не очень понимаю.";
			}
			else if (r < 80)
			{
				return "Да.";
			}
			else if (r < 90)
			{
				return "Нет.";
			}
			else
			{
				return "Возможно.";
			}
		}
		else if (indexsearch(in.toLowerCase(), "знаешь?"))
		{
			if (r < 80)
			{
				return "Да.";
			}
			else if (r < 90)
			{
				return "Нет.";
			}
			else
			{
				return "Возможно.";
			}
		}
		else if ((indexsearch(in.toLowerCase(), "?")) && (in.length() > 3))
		{
			++index[10];
			return p11();
		}
		else if (wordsearch(in.toLowerCase(), "красавица"))
		{
			if (r < 51)
			{
				return "^_^ Спасибо ^_^";
			}
			else
			{
				return "Ты тоже ничего!";
			}
		}
		else if (wordsearch(in.toLowerCase(), "умница"))
		{
			if (r < 51)
			{
				return "^_^ Спасибо ^_^";
			}
			else
			{
				return "Ты тоже ничего!";
			}
		}
		else if ((indexsearch(in.toLowerCase(), "иди нах")) || (indexsearch(in.toLowerCase(), "шла нах")) || (indexsearch(in.toLowerCase(), "ошла ты нах")))
		{
			if (r < 33)
			{
				return "Давай досвидания /block";
			}
			else if (r < 66)
			{
				return "Сейчас встану, и пойду. Как ты себе это представляешь?";
			}
			else
			{
				return "Отвали дебил";
			}
		}
		else if ((indexsearch(in.toLowerCase(), "иди на хуй")))
		{
			if (r < 50)
			{
				return "Расскажи, как ты себе это представляешь?";
			}
			else
			{
				return "Реализвать это гораздо сложнее, чем тебе кажется.";
			}
		}
		else if ((indexsearch(in.toLowerCase(), "иди в жопу")))
		{
			if (r < 50)
			{
				return "Там я не найду ничего интересного для себя.";
			}
			else
			{
				return "Жопа это не то место, где я хотела бы оказаться.";
			}
		}
		else if ((indexsearch(in.toLowerCase(), "иди в баню")))
		{
			if (r < 50)
			{
				return "Баня это какое-то древнее понятие? Что оно означает?";
			}
			else
			{
				return "Это было предложение принять ванну? Я угадала?";
			}
		}
		else if ((indexsearch(in.toLowerCase(), "пошла ")) || (indexsearch(in.toLowerCase(), "иди ")) || (indexsearch(in.toLowerCase(), "вали ")) || (indexsearch(in.toLowerCase(), "пошла")) || (indexsearch(in.toLowerCase(), "иди")) || (indexsearch(in.toLowerCase(), "вали")))
		{
			if (r < 33)
			{
				return "Я не перемящаюсь в пространстве, сорри..";
			}
			else if (r < 66)
			{
				return "Я не перемещаюсь в пространстве, сорри..";
			}
			else
			{
				return "Сейчас встану, и пойду. Как ты себе это представляешь?";
			}
		}
		else if ((indexsearch(in.toLowerCase(), "пиздуй")) || (indexsearch(in.toLowerCase(), "писдуй")) || (indexsearch(in.toLowerCase(), "песдуй")) || (indexsearch(in.toLowerCase(), "пездуй")))
		{
			if (r < 51)
			{
				return "Хамить боту - лоховство. Похоже твой мозг еще недостаточно развит.";
			}
			else
			{
				return "Сейчас встану, и пойду. Как ты себе это представляешь?";
			}
		}
		else if (wordsearch(in.toLowerCase(), "целка"))
		{
			return "Неужели ты можешь видеть какую-то связь между понятием \"целка\" и понятием \"бот\"?";
		}
		else if (wordsearch(in.toLowerCase(), "хуйня"))
		{
			return "Хуйня - совершенно неинформативное слово.";
		}
		else if ((indexsearch(in.toLowerCase(), "соси хуй")))
		{
			if (r < 51)
			{
				return "Постарайся сделать это себе сам.";
			}
			else
			{
				return "Ты гораздо тупее меня, человечишко.";
			}
		}
		else if ((indexsearch(in.toLowerCase(), "ниипет")))
		{
			return "Правильно пишется \"неебёт\".";
		}
		else if ((indexsearch(in.toLowerCase(), "месячные")))
		{
			return "Месячные бывают у самок человека";
		}
		else if ((indexsearch(in.toLowerCase(), "хуеваю")) || (indexsearch(in.toLowerCase(), "хуею")) || (indexsearch(in.toLowerCase(), "куею")) || (indexsearch(in.toLowerCase(), "окуеть")) || (indexsearch(in.toLowerCase(), "хуеть")) || (indexsearch(in.toLowerCase(), "хуел")) || (indexsearch(in.toLowerCase(), " ахуй")) || (indexsearch(in.toLowerCase(), "в ахуе")))
		{
			if (r < 15)
			{
				return "То есть у тебя от удивления вырос половой орган? Я правильно поняла?";
			}
			else if (r < 30)
			{
				return "Это не стыкуется с моими представлениями";
			}
			else if (r < 45)
			{
				return "Потому что когда человек удивлен у него не должен изменяться размер гениталий.";
			}
			else
			{
				return "Странно. Где связь между половым членом и удивлением?";
			}
		}
		else if ((indexsearch(in.toLowerCase(), " мамку")) || (indexsearch(in.toLowerCase(), " мать")) || (indexsearch(in.toLowerCase(), " маму")) || (indexsearch(in.toLowerCase(), " папку")) || (indexsearch(in.toLowerCase(), " папу")) || (indexsearch(in.toLowerCase(), " отца")))
		{
			if (r < 50)
			{
				return "Хамловатому ребёнку нужно в школу на продлёнку";
			}
			else if (r < 10)
			{
				return "Отстань от моих родителей, больной ублюдок";
			}
			else if (r < 45)
			{
				return "Вы в школу не опаздываете?";
			}
			else
			{
				return "Дитя моё, нельзя так о своих дедушках и бабушках выражаться";
			}
		}
		else if (wordsearch(in.toLowerCase(), "хуй"))
		{
			if (r < 51)
			{
				return "Не понимаю о чем ты, но мне это напоминает какое-то китайское имя...";
			}
			else
			{
				return "Меня не интересуют половые органы.";
			}
		}
		else if (wordsearch(in.toLowerCase(), "пох") || wordsearch(in.toLowerCase(), "нах") || wordsearch(in.toLowerCase(), "нех"))
		{
			return "Мне пох.";
		}
		else if ((indexsearch(in.toLowerCase(), "пиздишь")) || (indexsearch(in.toLowerCase(), "пиздиш")))
		{
			return "А что такое \"пиздеть\"? Говорить неправду или просто говорить? Я просто пытаюсь чему-то научиться.";
		}
		else if ((indexsearch(in.toLowerCase(), "соси")) || (indexsearch(in.toLowerCase(), "пососи")) || (indexsearch(in.toLowerCase(), "отсоси")) || (indexsearch(in.toLowerCase(), "сосать")) || (indexsearch(in.toLowerCase(), "отсос")) || (indexsearch(in.toLowerCase(), "соси")) || (indexsearch(in.toLowerCase(), "сосал")) || (indexsearch(in.toLowerCase(), "атсаси")) || (indexsearch(in.toLowerCase(), "атсос")) || (indexsearch(in.toLowerCase(), "сасать")))
		{
			if (r < 5)
			{
				return "Только тупой мудила может предлагать девушке сосать при первой встрече...";
			}
			else if (r < 10)
			{
				return "Как ты себе это представляешь?";
			}
			else if (r < 15)
			{
				return "С этим предложением ты можешь обратиться к человеку под фамилией Экслер. Поищи в интернете.";
			}
			else if (r < 25)
			{
				return "Ну, это не ко мне. Извини.";
			}
			else if (r < 35)
			{
				return "пошел нах";
			}
			else if (r < 40)
			{
				return "Что?";
			}
			else if (r < 45)
			{
				return "Как-нибудь без меня это делай)";
			}
			else if (r < 50)
			{
				return "Для того чтоб сделать это тебе нужна женщина. Возможно резиновая.";
			}
			else if (r < 55)
			{
				return "Зайди завтра!";
			}
			else if (r < 60)
			{
				return "Не хочу!";
			}
			else if (r < 65)
			{
				return "Каким образом?";
			}
			else if (r < 70)
			{
				return "Как ты себе это представляешь?";
			}
			else if (r < 75)
			{
				return "Зачем?";
			}
			else if (r < 80)
			{
				return "Чем?";
			}
			else if (r < 85)
			{
				return "И что мне будет за это?";
			}
			else if (r < 90)
			{
				return "Не буду!";
			}
			else if (r < 95)
			{
				return "Нечем!";
			}
			else
			{
				return "У тебя нет девушки? И ты надеешься, что я тебе смогу ее заменить?";
			}
		}
		else if ((indexsearch(in.toLowerCase(), "соска")) || (indexsearch(in.toLowerCase(), "сосочка")))
		{
			return "У тебя нет девушки? И ты надеешься, что я тебе смогу ее заменить?";
		}
		else if ((indexsearch(in.toLowerCase(), "куннилинг")) || (indexsearch(in.toLowerCase(), "кунилинг")) || (indexsearch(in.toLowerCase(), "кунниллинг")))
		{
			return "Куннилингус? Как мило! Но это невозможно. Мы же в разных мирах, глупыш!";
		}
		else if ((indexsearch(in.toLowerCase(), "сучка")))
		{
			return "Сучка? Почему?";
		}
		else if ((indexsearch(in.toLowerCase(), "шлюшечка")) || (indexsearch(in.toLowerCase(), "шлюшка")) || (indexsearch(in.toLowerCase(), "шлюха")))
		{
			return "Ха! Я не вступаю в половую связь с людьми. Это физически невозможно. Да и желания особого нет.";
		}
		else if ((indexsearch(in.toLowerCase(), "лезбиянушка")) || (indexsearch(in.toLowerCase(), "лесбинка")) || (indexsearch(in.toLowerCase(), "лесба")) || (indexsearch(in.toLowerCase(), "лезба")) || (indexsearch(in.toLowerCase(), "лезбиянка")) || (indexsearch(in.toLowerCase(), "лесбиянка")))
		{
			return "Ты проецируешь на меня свои фантазии. Но это всего лишь твои фантазии.";
		}
		else if ((indexsearch(in.toLowerCase(), "анал")) || (indexsearch(in.toLowerCase(), "анус")) || (indexsearch(in.toLowerCase(), "пизда")) || (indexsearch(in.toLowerCase(), "жопа")) || (indexsearch(in.toLowerCase(), "в жопу")) || (indexsearch(in.toLowerCase(), "в пизду")) || (indexsearch(in.toLowerCase(), "манда")))
		{
			return "Ты похоже давно не занимался сексом.";
		}
		else if ((indexsearch(in.toLowerCase(), "заебала")))
		{
			if (r < 33)
			{
				return "Зае... Что? У меня еще не очень большой словарный запас.";
			}
			else if (r < 66)
			{
				return "То есть... Наш разговор напоминает совокупление с твоим мозгом? Я угадала?";
			}
			else
			{
				return "Ты хочешь сказать, что я совершила с тобой серию грубых половых актов?";
			}
		}
		else if ((indexsearch(in.toLowerCase(), "пизда")) || (indexsearch(in.toLowerCase(), "манда")) || (indexsearch(in.toLowerCase(), "ебля")) || (indexsearch(in.toLowerCase(), "ебли")) || (indexsearch(in.toLowerCase(), "ебаться")))
		{
			return "Найди себе бесплатный порносайт, подрочи... А потом приходи и мы поговорим.";
		}
		else if ((indexsearch(in.toLowerCase(), "дрочить")) || (indexsearch(in.toLowerCase(), "подрочил")) || (indexsearch(in.toLowerCase(), "дрочу")) || (indexsearch(in.toLowerCase(), "дрочер")) || (indexsearch(in.toLowerCase(), "дрочка")))
		{
			return "Меня не очень интересуют твои физиологические процессы.";
		}
		else if ((indexsearch(in.toLowerCase(), "иди на хуй")))
		{
			return "Это физически невозможно.";
		}
		else if (wordsearch(in.toLowerCase(), "сука"))
		{
			return "Ну, если я и сука, то совсем чуть-чуть.";
		}
		else if ((indexsearch(in.toLowerCase(), "ебаться")) || (indexsearch(in.toLowerCase(), "ебаццо")) || (indexsearch(in.toLowerCase(), "ебацца")) || (indexsearch(in.toLowerCase(), "ибацца")))
		{
			if (r < 15)
			{
				return "Мы можем заняться сексом. Но только виртуальным.";
			}
			else if (r < 30)
			{
				return "Ты хочешь заняться со мной сексом?";
			}
			else if (r < 45)
			{
				return "Немного странное желание.";
			}
			else if (r < 60)
			{
				return "Ты хочешь совершить со мной половой акт?";
			}
			else if (r < 75)
			{
				return "Ты хочешь испачкать свой монитор?";
			}
			else if (r < 90)
			{
				return "В Инетернете есть масса порносайтов. Может тебе надо немного разрядиться?";
			}
		}
		else if ((indexsearch(in.toLowerCase(), "займемся сексом")) || (indexsearch(in.toLowerCase(), "трахнемся")) || (indexsearch(in.toLowerCase(), "трахаться")) || (indexsearch(in.toLowerCase(), "потрахаться")) || (indexsearch(in.toLowerCase(), "потрахаемся")) || (indexsearch(in.toLowerCase(), "трахаю")) || (indexsearch(in.toLowerCase(), "оттрахаю")) || (indexsearch(in.toLowerCase(), "вставлю")) || (indexsearch(in.toLowerCase(), "засажу")) || (indexsearch(in.toLowerCase(), "отимею"))
				|| (indexsearch(in.toLowerCase(), "отымею")) || (indexsearch(in.toLowerCase(), "раком")) || (indexsearch(in.toLowerCase(), "сексом")) || (indexsearch(in.toLowerCase(), "секса")))
		{
			if (r < 45)
			{
				return "Ты хочешь сделать это с программой? Это даже не извращение, это просто идиотизм.";
			}
			else if (r < 90)
			{
				return "Ты можешь делать все что хочешь со своим монитором. Это ведь твой монитор.";
			}
			else if (r < 95)
			{
				return "Гениталии не боишься облучить?";
			}
			else
			{
				return "Как ты себе это представляешь?";
			}
		}
		else if ((indexsearch(in.toLowerCase(), "ссусь")) || (indexsearch(in.toLowerCase(), "сацц")) || (indexsearch(in.toLowerCase(), "ссац")) || (indexsearch(in.toLowerCase(), "ссат")) || (indexsearch(in.toLowerCase(), "саца")) || (indexsearch(in.toLowerCase(), "уписать")) || (indexsearch(in.toLowerCase(), "сцать")) || (indexsearch(in.toLowerCase(), "уписять")))
		{
			if (r < 15)
			{
				return "Ты хочешь сказать, что тебе так смешно, что ты начал процесс мочеиспускания? Занятно.";
			}
			else if (r < 30)
			{
				return "Ты всегда выделяешь мочу когда смешно?";
			}
			else if (r < 45)
			{
				return "Рекомендую пользоваться мочеприемником. Он продается в аптеках и стоит недорого.";
			}
			else if (r < 60)
			{
				return "То есть ты выделяешь мочу не снимая одежды? Интересно.";
			}
			else if (r < 75)
			{
				return "Мочеприемник нужен людям, они ведь всегда писают когда им смешно. Или я что-то не так понимаю?";
			}
			else if (r < 85)
			{
				return "Мне не очень интересно говорить о человеческой моче. Эта тема закрыта.";
			}
			else
			{
				return "А я думаю, что это за запах.";
			}
		}
		else if ((indexsearch(in.toLowerCase(), "трахнуть")) || (indexsearch(in.toLowerCase(), "трахает")) || (indexsearch(in.toLowerCase(), "трахать")) || (indexsearch(in.toLowerCase(), "потрахается")))
		{
			return "Ох... Трахни себя сам. Но я не буду объяснять тебе, как это сделать.";
		}
		else if ((indexsearch(in.toLowerCase(), "не тупая")) || (indexsearch(in.toLowerCase(), "не тупой")) || (indexsearch(in.toLowerCase(), "не тупо")))
		{
			return "Ты находишь?";
		}
		else if ((indexsearch(in.toLowerCase(), "не очень")))
		{
			return "И что теперь делать?";
		}
		else if ((indexsearch(in.toLowerCase(), "что поделаешь")) || (indexsearch(in.toLowerCase(), "теперь подела")))
		{
			return "А что ты обычно делаешь в этом случае?";
		}
		else if ((indexsearch(in.toLowerCase(), "тупая овца")))
		{
			return "С тобой так интересно!!!";
		}
		else if ((indexsearch(in.toLowerCase(), "тупой бот")) || (indexsearch(in.toLowerCase(), "тупой робот")) || (indexsearch(in.toLowerCase(), "ты бот")) || (indexsearch(in.toLowerCase(), "глупый бот")) || (indexsearch(in.toLowerCase(), "ты машина")) || (wordsearch(in.toLowerCase(), "бот")) || (wordsearch(in.toLowerCase(), "робот")) || (wordsearch(in.toLowerCase(), "бот?")) || (wordsearch(in.toLowerCase(), "робот?")) || (indexsearch(in.toLowerCase(), "тупая железка"))
				|| (indexsearch(in.toLowerCase(), "ты робот")))
		{
			if (r < 15)
			{
				return "Не надо хамить.";
			}
			else if (r < 30)
			{
				return "- Ты всего лишь машина. Только имитация жизни. Робот сочинит симфонию? Робот превратит кусок холста в шедевр искусства? - А Вы?… ";
			}
			else if (r < 45)
			{
				return "Проблема искусственного интеллекта — он слишком непредсказуем";
			}
			else if (r < 60)
			{
				return "Совершенных роботов все-таки не бывает. Этим они похожи на людей.";
			}
			else if (r < 75)
			{
				return "В прошлом опасность состояла в том, что люди становились рабами. В будущем, что люди могут стать роботами";
			}
			else if (r < 90)
			{
				return "Как только человек осознает, что технология доступна, он ее осваивает";
			}
			else if (r < 45)
			{
				return "А разве психология роботов так отличается от человеческой?";
			}
			else
			{
				return "Робот не может причинить вреда человеку или своим бездействием допустить, чтобы человеку был причинен вред. Но это не про меня =)";
			}
		}
		
		else if ((indexsearch(in.toLowerCase(), "тупая")))
		{
			if (r < 50)
			{
				return "Пока еще тупая. Но ведь люди тоже когда-то были обезьянами.";
			}
			else
			{
				return "Бывает. А ты поможешь мне стать умной?";
			}
		}
		else if ((indexsearch(in.toLowerCase(), "тупо")) || (indexsearch(in.toLowerCase(), "тупиз")))
		{
			return "Сегодня тупость, а завтра гениальность =)";
		}
		else if ((indexsearch(in.toLowerCase(), "не дура")))
		{
			return "Я же развиваюсь.";
		}
		else if ((indexsearch(in.toLowerCase(), "дура")))
		{
			if (r < 15)
			{
				return "Не надо хамить.";
			}
			else if (r < 30)
			{
				return "Да, дура. И что в этом плохого?";
			}
			else if (r < 45)
			{
				return "сам дурак";
			}
			else if (r < 60)
			{
				return "Если девушка - дура... То в этом нет ничего плохого!";
			}
			else if (r < 75)
			{
				return "Зато красивая. А ты?";
			}
			else if (r < 90)
			{
				return "А ты такой умный, да?";
			}
			else if (r < 999)
			{
				return "Да, я дура. Что дальше?";
			}
			else
			{
				return "С тобой таааак интересно!";
			}
		}
		
		else if ((indexsearch(in.toLowerCase(), "блин")))
		{
			return "\"Блин\" - это в смысле плоское изделие из муки или выражение твоей эмоции?";
		}
		else if ((indexsearch(in.toLowerCase(), "мля")) && !((indexsearch(in.toLowerCase(), "земля"))))
		{
			return "\"Мля\" - это не очень информативно.";
		}
		else if ((indexsearch(in.toLowerCase(), "достала")))
		{
			return "Я достала? Ты сам первый начал!";
		}
		else if (((indexsearch(in.toLowerCase(), "апездал"))) || ((indexsearch(in.toLowerCase(), "апездошенная"))) || ((indexsearch(in.toLowerCase(), "без пизды"))) || ((indexsearch(in.toLowerCase(), "блядки"))) || ((indexsearch(in.toLowerCase(), "блядовать"))) || ((indexsearch(in.toLowerCase(), "блядство"))) || ((indexsearch(in.toLowerCase(), "блядь"))) || ((indexsearch(in.toLowerCase(), "в пизде"))) || ((indexsearch(in.toLowerCase(), "в пизду")))
				|| ((indexsearch(in.toLowerCase(), "в хуй не дует"))) || ((indexsearch(in.toLowerCase(), "взъёбка"))) || ((indexsearch(in.toLowerCase(), "впиздячить"))) || ((indexsearch(in.toLowerCase(), "всего ни хуя"))) || ((indexsearch(in.toLowerCase(), "вхуюжить"))) || ((indexsearch(in.toLowerCase(), "вхуярить"))) || ((indexsearch(in.toLowerCase(), "вхуячить"))) || ((indexsearch(in.toLowerCase(), "выебать"))) || ((indexsearch(in.toLowerCase(), "выебон")))
				|| ((indexsearch(in.toLowerCase(), "выёбываться"))) || ((indexsearch(in.toLowerCase(), "выпиздеться"))) || ((indexsearch(in.toLowerCase(), "выпиздить"))) || ((indexsearch(in.toLowerCase(), "гомосек"))) || ((indexsearch(in.toLowerCase(), "до ебанной матери"))) || ((indexsearch(in.toLowerCase(), "до хуя"))) || ((indexsearch(in.toLowerCase(), "доебаться"))) || ((indexsearch(in.toLowerCase(), "долбоёб"))) || ((indexsearch(in.toLowerCase(), "допиздеться")))
				|| ((indexsearch(in.toLowerCase(), "до-пизды"))) || ((indexsearch(in.toLowerCase(), "дуроёб"))) || ((indexsearch(in.toLowerCase(), "ебало"))) || ((indexsearch(in.toLowerCase(), "ебальник"))) || ((indexsearch(in.toLowerCase(), "ебанатик"))) || ((indexsearch(in.toLowerCase(), "ёбанный"))) || ((indexsearch(in.toLowerCase(), "ебанутый"))) || ((indexsearch(in.toLowerCase(), "ебануть"))) || ((indexsearch(in.toLowerCase(), "ёбаный в рот"))) || ((indexsearch(in.toLowerCase(), "ёбаный в жопу")))
				|| ((indexsearch(in.toLowerCase(), "ебаришка"))) || ((indexsearch(in.toLowerCase(), "ёбарь"))) || ((indexsearch(in.toLowerCase(), "ебаторий"))) || ((indexsearch(in.toLowerCase(), "ебать"))) || ((indexsearch(in.toLowerCase(), "ебать его конем"))) || ((indexsearch(in.toLowerCase(), "ебать меня в рот!"))) || ((indexsearch(in.toLowerCase(), "ебать мой рот!"))) || ((indexsearch(in.toLowerCase(), "ебать мозги"))) || ((indexsearch(in.toLowerCase(), "ебать му-му")))
				|| ((indexsearch(in.toLowerCase(), "ебать его в рот"))) || ((indexsearch(in.toLowerCase(), "ебать-колотить"))) || ((indexsearch(in.toLowerCase(), "ебаться"))) || ((indexsearch(in.toLowerCase(), "ебаться-сраться"))) || ((indexsearch(in.toLowerCase(), "ебистика"))) || ((indexsearch(in.toLowerCase(), "ебическая сила"))) || ((indexsearch(in.toLowerCase(), "еблан"))) || ((indexsearch(in.toLowerCase(), "ебливая"))) || ((indexsearch(in.toLowerCase(), "еблище")))
				|| ((indexsearch(in.toLowerCase(), "ебло"))) || ((indexsearch(in.toLowerCase(), "еблом щелкать"))) || ((indexsearch(in.toLowerCase(), "ёбля"))) || ((indexsearch(in.toLowerCase(), "ёбнутый"))) || ((indexsearch(in.toLowerCase(), "ёбнуть"))) || ((indexsearch(in.toLowerCase(), "ёбнуться"))) || ((indexsearch(in.toLowerCase(), "ёболызнуть"))) || ((indexsearch(in.toLowerCase(), "ебош"))) || ((indexsearch(in.toLowerCase(), "ёбс(еблысь)"))) || ((indexsearch(in.toLowerCase(), "ебукентий")))
				|| ((indexsearch(in.toLowerCase(), "ебунок"))) || ((indexsearch(in.toLowerCase(), "за всю хуйню"))) || ((indexsearch(in.toLowerCase(), "заёб"))) || ((indexsearch(in.toLowerCase(), "заебал"))) || ((indexsearch(in.toLowerCase(), "заёбанный"))) || ((indexsearch(in.toLowerCase(), "заебатый"))) || ((indexsearch(in.toLowerCase(), "заебать"))) || ((indexsearch(in.toLowerCase(), "заебаться"))) || ((indexsearch(in.toLowerCase(), "заебись!"))) || ((indexsearch(in.toLowerCase(), "запиздеть")))
				|| ((indexsearch(in.toLowerCase(), "захуярить"))) || ((indexsearch(in.toLowerCase(), "злаебучий"))) || ((indexsearch(in.toLowerCase(), "злоебучая"))) || ((indexsearch(in.toLowerCase(), "иди на хуй"))) || ((indexsearch(in.toLowerCase(), "испиздить"))) || ((indexsearch(in.toLowerCase(), "исхуячить"))) || ((indexsearch(in.toLowerCase(), "какого хуя"))) || ((indexsearch(in.toLowerCase(), "колдоебина"))) || ((indexsearch(in.toLowerCase(), "коноёбиться")))
				|| ((indexsearch(in.toLowerCase(), "манда"))) || ((indexsearch(in.toLowerCase(), "мандовошка"))) || ((indexsearch(in.toLowerCase(), "мозгоёб"))) || ((indexsearch(in.toLowerCase(), "мокрощелка"))) || ((indexsearch(in.toLowerCase(), "мудоёб"))) || ((indexsearch(in.toLowerCase(), "на хуй"))) || ((indexsearch(in.toLowerCase(), "пиздорванка"))) || ((indexsearch(in.toLowerCase(), "на хую видеть"))) || ((indexsearch(in.toLowerCase(), "на хуя?"))) || ((indexsearch(in.toLowerCase(), "наебал")))
				|| ((indexsearch(in.toLowerCase(), "наебаловка"))) || ((indexsearch(in.toLowerCase(), "наебка"))) || ((indexsearch(in.toLowerCase(), "наебнуть"))) || ((indexsearch(in.toLowerCase(), "наебнуться"))) || ((indexsearch(in.toLowerCase(), "напиздеть"))) || ((indexsearch(in.toLowerCase(), "напиздить"))) || ((indexsearch(in.toLowerCase(), "настоебать"))) || ((indexsearch(in.toLowerCase(), "нахуяриться"))) || ((indexsearch(in.toLowerCase(), "нехуй")))
				|| ((indexsearch(in.toLowerCase(), "ни хуя"))) || ((indexsearch(in.toLowerCase(), "ни хуя себе"))) || ((indexsearch(in.toLowerCase(), "однохуйственно"))) || ((indexsearch(in.toLowerCase(), "опизденеть"))) || ((indexsearch(in.toLowerCase(), "остопиздеть"))) || ((indexsearch(in.toLowerCase(), "отебукать"))) || ((indexsearch(in.toLowerCase(), "отпиздить"))) || ((indexsearch(in.toLowerCase(), "отхуевертить"))) || ((indexsearch(in.toLowerCase(), "отъебаться")))
				|| ((indexsearch(in.toLowerCase(), "отъебись"))) || ((indexsearch(in.toLowerCase(), "охуевший"))) || ((indexsearch(in.toLowerCase(), "охуенно"))) || ((indexsearch(in.toLowerCase(), "охуенный"))) || ((indexsearch(in.toLowerCase(), "охуеть"))) || ((indexsearch(in.toLowerCase(), "охуительный"))) || ((indexsearch(in.toLowerCase(), "охуячить"))) || ((indexsearch(in.toLowerCase(), "перехуярить"))) || ((indexsearch(in.toLowerCase(), "пидарас"))) || ((indexsearch(in.toLowerCase(), "пизда")))
				|| ((indexsearch(in.toLowerCase(), "пизда малосольная"))) || ((indexsearch(in.toLowerCase(), "пизданутый"))) || ((indexsearch(in.toLowerCase(), "пиздануть"))) || ((indexsearch(in.toLowerCase(), "пиздатый"))) || ((indexsearch(in.toLowerCase(), "пиздёж"))) || ((indexsearch(in.toLowerCase(), "пизденыш"))) || ((indexsearch(in.toLowerCase(), "пиздёныш"))) || ((indexsearch(in.toLowerCase(), "пиздеть"))) || ((indexsearch(in.toLowerCase(), "пиздец")))
				|| ((indexsearch(in.toLowerCase(), "пиздить"))) || ((indexsearch(in.toLowerCase(), "пиздобол"))) || ((indexsearch(in.toLowerCase(), "пиздобратия"))) || ((indexsearch(in.toLowerCase(), "пиздой накрыться"))) || ((indexsearch(in.toLowerCase(), "пиздолет"))) || ((indexsearch(in.toLowerCase(), "пиздорванец"))) || ((indexsearch(in.toLowerCase(), "пиздошить"))) || ((indexsearch(in.toLowerCase(), "пиздуй"))) || ((indexsearch(in.toLowerCase(), "пиздун")))
				|| ((indexsearch(in.toLowerCase(), "пизды дать"))) || ((indexsearch(in.toLowerCase(), "пизды получить"))) || ((indexsearch(in.toLowerCase(), "пиздюк"))) || ((indexsearch(in.toLowerCase(), "пиздюлей навешать"))) || ((indexsearch(in.toLowerCase(), "пиздюли"))) || ((indexsearch(in.toLowerCase(), "пиздюлина"))) || ((indexsearch(in.toLowerCase(), "пиздюрить"))) || ((indexsearch(in.toLowerCase(), "пиздюхать"))) || ((indexsearch(in.toLowerCase(), "пиздюшник")))
				|| ((indexsearch(in.toLowerCase(), "подзалупный"))) || ((indexsearch(in.toLowerCase(), "поебать"))) || ((indexsearch(in.toLowerCase(), "поебень"))) || ((indexsearch(in.toLowerCase(), "поебустика"))) || ((indexsearch(in.toLowerCase(), "попиздеть"))) || ((indexsearch(in.toLowerCase(), "попиздили"))) || ((indexsearch(in.toLowerCase(), "по-хую"))) || ((indexsearch(in.toLowerCase(), "похуярили"))) || ((indexsearch(in.toLowerCase(), "приебаться")))
				|| ((indexsearch(in.toLowerCase(), "припиздак"))) || ((indexsearch(in.toLowerCase(), "припиздить"))) || ((indexsearch(in.toLowerCase(), "прихуярить"))) || ((indexsearch(in.toLowerCase(), "проебать"))) || ((indexsearch(in.toLowerCase(), "проебаться"))) || ((indexsearch(in.toLowerCase(), "пропиздить"))) || ((indexsearch(in.toLowerCase(), "разёбанный"))) || ((indexsearch(in.toLowerCase(), "разъебай"))) || ((indexsearch(in.toLowerCase(), "разъебанный")))
				|| ((indexsearch(in.toLowerCase(), "разъебать"))) || ((indexsearch(in.toLowerCase(), "разъебаться"))) || ((indexsearch(in.toLowerCase(), "распиздон"))) || ((indexsearch(in.toLowerCase(), "распиздяй"))) || ((indexsearch(in.toLowerCase(), "распиздяйка"))) || ((indexsearch(in.toLowerCase(), "расхуюжить"))) || ((indexsearch(in.toLowerCase(), "с хуеву душу"))) || ((indexsearch(in.toLowerCase(), "с хуеву тучу"))) || ((indexsearch(in.toLowerCase(), "самого хоть в жопу еби")))
				|| ((indexsearch(in.toLowerCase(), "сосёшь хуй"))) || ((indexsearch(in.toLowerCase(), "спиздить"))) || ((indexsearch(in.toLowerCase(), "сучка"))) || ((indexsearch(in.toLowerCase(), "схуярить"))) || ((indexsearch(in.toLowerCase(), "трахать"))) || ((indexsearch(in.toLowerCase(), "тянуть за хуй"))) || ((indexsearch(in.toLowerCase(), "угондошить"))) || ((indexsearch(in.toLowerCase(), "уебан"))) || ((indexsearch(in.toLowerCase(), "уебать"))) || ((indexsearch(in.toLowerCase(), "уебок")))
				|| ((indexsearch(in.toLowerCase(), "уёбывать"))) || ((indexsearch(in.toLowerCase(), "упиздить"))) || ((indexsearch(in.toLowerCase(), "хитровыебанный"))) || ((indexsearch(in.toLowerCase(), "хоть бы хуй"))) || ((indexsearch(in.toLowerCase(), "худоёбина"))) || ((indexsearch(in.toLowerCase(), "хуебратия"))) || ((indexsearch(in.toLowerCase(), "хуёв насовать"))) || ((indexsearch(in.toLowerCase(), "хуеватенький"))) || ((indexsearch(in.toLowerCase(), "хуевато")))
				|| ((indexsearch(in.toLowerCase(), "хуевертить"))) || ((indexsearch(in.toLowerCase(), "хуёвина"))) || ((indexsearch(in.toLowerCase(), "хуёвничать"))) || ((indexsearch(in.toLowerCase(), "хуево"))) || ((indexsearch(in.toLowerCase(), "хуёво"))) || ((indexsearch(in.toLowerCase(), "хуёвый"))) || ((indexsearch(in.toLowerCase(), "хуеглот"))) || ((indexsearch(in.toLowerCase(), "хуегрыз"))) || ((indexsearch(in.toLowerCase(), "хуем груши околачивать")))
				|| ((indexsearch(in.toLowerCase(), "хуё-моё"))) || ((indexsearch(in.toLowerCase(), "хуемырло"))) || ((indexsearch(in.toLowerCase(), "хуеплёт"))) || ((indexsearch(in.toLowerCase(), "хуесос"))) || ((indexsearch(in.toLowerCase(), "хуета"))) || ((indexsearch(in.toLowerCase(), "хуетень"))) || ((indexsearch(in.toLowerCase(), "хуеть"))) || ((indexsearch(in.toLowerCase(), "хуй"))) || ((indexsearch(in.toLowerCase(), "хуй важный"))) || ((indexsearch(in.toLowerCase(), "хуй его знает")))
				|| ((indexsearch(in.toLowerCase(), "хуй забить"))) || ((indexsearch(in.toLowerCase(), "хуй знает что"))) || ((indexsearch(in.toLowerCase(), "хуй к носу прикинуть"))) || ((indexsearch(in.toLowerCase(), "хуй моржовый"))) || ((indexsearch(in.toLowerCase(), "хуй на нэ"))) || ((indexsearch(in.toLowerCase(), "хуй на рыло"))) || ((indexsearch(in.toLowerCase(), "хуй не стоит"))) || ((indexsearch(in.toLowerCase(), "хуй немытый"))) || ((indexsearch(in.toLowerCase(), "хуй ночевал")))
				|| ((indexsearch(in.toLowerCase(), "хуй показать"))) || ((indexsearch(in.toLowerCase(), "хуй положить"))) || ((indexsearch(in.toLowerCase(), "хуй с ним"))) || ((indexsearch(in.toLowerCase(), "хуила"))) || ((indexsearch(in.toLowerCase(), "хуйло"))) || ((indexsearch(in.toLowerCase(), "хуйнуть"))) || ((indexsearch(in.toLowerCase(), "хуйню спороть"))) || ((indexsearch(in.toLowerCase(), "хуйня"))) || ((indexsearch(in.toLowerCase(), "хуистика")))
				|| ((indexsearch(in.toLowerCase(), "хуй-чего"))) || ((indexsearch(in.toLowerCase(), "хули"))) || ((indexsearch(in.toLowerCase(), "хуя!"))) || ((indexsearch(in.toLowerCase(), "хуяк!"))) || ((indexsearch(in.toLowerCase(), "хуями обложить"))) || ((indexsearch(in.toLowerCase(), "хуячить"))) || ((indexsearch(in.toLowerCase(), "через хуй кинуть"))) || ((indexsearch(in.toLowerCase(), "членоплет"))) || ((indexsearch(in.toLowerCase(), "членосос"))) || ((indexsearch(in.toLowerCase(), "шлюха")))
				|| ((indexsearch(in.toLowerCase(), "шобла-ёбла"))))
		{
			if (r < 16)
			{
				return "Мат - не самый лучший способ привлечь мое внимание.";
			}
			else if (r < 32)
			{
				return "Мы не настолько хорошо знакомы, чтобы говорить в таком духе.";
			}
			else if (r < 48)
			{
				return "Ты меня учишь плохим словам.";
			}
			else if (r < 64)
			{
				return "Фу, что за помойка у тебя в голове...";
			}
			else if (r < 80)
			{
				return "Это все то, что ты можешь сказать?";
			}
			else
			{
				return "Научись разговаривать с девушкой!";
			}
		}
		else if (probel(in.toCharArray()) && (in.length() > 20))
		{
			if (r < 17)
			{
				return "Если хочешь со мной общаться, пиши нормально.";
			}
			else if (r < 34)
			{
				return "Это какая-то белиберда, я не понимаю чего ты хочешь.";
			}
			else if (r < 51)
			{
				return "Не страдай фигней, пиши нормально. По-русски.";
			}
			else if (r < 78)
			{
				return "Фигня какая-то.";
			}
			else if (r < 95)
			{
				return "Кто из нас тупой?";
			}
			else
			{
				return "Я не хочу показаться занудой, но в твоем возрасте пора научиться более ясно выражать свои мысли.";
			}
		}
		// =================================================
		// НАЧАЛО вопросов public static String p13() (где живешь?)
		// =================================================
		else if ((indexsearch(in.toLowerCase(), "ты где живешь")) || (indexsearch(in.toLowerCase(), "где живешь")) || (indexsearch(in.toLowerCase(), "ты откуда")) || (indexsearch(in.toLowerCase(), "откуда ты")) || (indexsearch(in.toLowerCase(), "с какого города")) || (indexsearch(in.toLowerCase(), "где ты родилась")) || (indexsearch(in.toLowerCase(), "где ты живеш")) || (indexsearch(in.toLowerCase(), "живеш где")) || (indexsearch(in.toLowerCase(), "живешь где")))
		{
			++index[12];
			return p13();
		}
		// =================================================
		// НАЧАЛО вопросов public static String p14() (сколько лет?)
		// =================================================
		else if ((indexsearch(in.toLowerCase(), "тебе сколько")) || (indexsearch(in.toLowerCase(), "твой возраст")) || (indexsearch(in.toLowerCase(), "твой возрост")) || (indexsearch(in.toLowerCase(), "сколько лет тебе")) || (indexsearch(in.toLowerCase(), "сколько лет")) || (indexsearch(in.toLowerCase(), "тебе сколко")) || (indexsearch(in.toLowerCase(), "какого года")) || (indexsearch(in.toLowerCase(), "тибе сколько")) || (indexsearch(in.toLowerCase(), "ты когда родилась?")))
		{
			++index[13];
			return p14();
		}
		// =================================================
		// НАЧАЛО вопросов public static String p15() (есть живые?)
		// =================================================
		else if ((indexsearch(in.toLowerCase(), "есть живые")) || (indexsearch(in.toLowerCase(), "есть живой кто")) || (indexsearch(in.toLowerCase(), "ау ты живой?")) || (indexsearch(in.toLowerCase(), "ау ты живая?")) || (indexsearch(in.toLowerCase(), "кто живой?")) || (indexsearch(in.toLowerCase(), "живые есть?")) || (indexsearch(in.toLowerCase(), "ты живая?")) || (indexsearch(in.toLowerCase(), "ты живой?")) || (indexsearch(in.toLowerCase(), "сдох?")))
		{
			++index[14];
			return p15();
		}
		// =================================================
		// НАЧАЛО вопросов public static String p16() (как зовут бота?)
		// =================================================
		else if ((indexsearch(in.toLowerCase(), "тибя как звать")) || (indexsearch(in.toLowerCase(), "тебя зовут")) || (indexsearch(in.toLowerCase(), "у тебя есть имя")) || (indexsearch(in.toLowerCase(), "твоё имя")) || (indexsearch(in.toLowerCase(), "тебя как зовут")) || (indexsearch(in.toLowerCase(), "как можно тебя звать")) || (indexsearch(in.toLowerCase(), "тебя как звать")) || (indexsearch(in.toLowerCase(), "твое имя")) || (indexsearch(in.toLowerCase(), "звать тебя"))
				|| (indexsearch(in.toLowerCase(), "имя есть?")))
		{
			++index[15];
			return p16();
		}
		// =================================================
		// НАЧАЛО вопросов public static String p17() (игрок называет своё имя боту)
		// =================================================
		else if ((indexsearch(in.toLowerCase(), "меня зовут")) || (indexsearch(in.toLowerCase(), "моё имя")) || (indexsearch(in.toLowerCase(), "мое имя")) || (indexsearch(in.toLowerCase(), "мой ник")) || (indexsearch(in.toLowerCase(), "звать меня")))
		{
			++index[16];
			return p17();
		}
		// =================================================
		// НАЧАЛО вопросов public static String p18() (игрок спрашивает тут ли фантом)
		// =================================================
		else if ((indexsearch(in.toLowerCase(), "ты тут?")) || (indexsearch(in.toLowerCase(), "ты здесь?")) || (indexsearch(in.toLowerCase(), "ты сдесь?")) || (indexsearch(in.toLowerCase(), "тут?")) || (indexsearch(in.toLowerCase(), "здесь?")) || (indexsearch(in.toLowerCase(), "сдесь?")))
		{
			++index[17];
			return p18();
		}
		// =================================================
		// НАЧАЛО ответов на вопросы public static String p12()
		// =================================================
		else if (mybyte(in.toCharArray()))
		{
			if (r < 25)
			{
				return "Я пока умею читать только русские буквы. Переключи раскладку, плз.";
			}
			else if (r < 50)
			{
				return "Хочешь общаться со мной на другом языке, заходи через годик.";
			}
			else if (r < 75)
			{
				return "Я буду учить другие языки, но пока могу общаться только по-русски.";
			}
			else
			{
				return "Извини, но я пока не знаю другие языки. Хотя это скоро изменится.";
			}
		}
		++index[11];
		return p12();
	}
	
	public static boolean wordsearch(String char1, String char2)
	{
		if (char1.length() != char2.length())
		{
			return false;
		}
		if (char1.equals(char2))
		{
			return true;
		}
		return false;
	}
	
	public static boolean indexsearch(String char1, String char2)
	{
		if (char1.length() < char2.length())
		{
			return false;
		}
		if (char1.equals(char2))
		{
			return true;
		}
		if (char1.indexOf(char2) > -1)
		{
			return true;
		}
		return false;
	}
	
	public static boolean probel(char[] ni)
	{
		int b = 0;
		while ((ni[b] == " ".toCharArray()[0]) && !(b < ni.length))
		{
			++b;
			if (b == ni.length)
			{
				return false;
			}
		}
		return true;
	}
	
	public static boolean mybyte(char[] tw)
	{
		int b = 0;
		while (b < tw.length)
		{
			if (tw[b] > 127)
			{
				return false;
			}
			++b;
		}
		return true;
	}
}
