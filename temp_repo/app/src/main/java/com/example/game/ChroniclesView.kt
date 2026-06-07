package com.example.game

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class CityLore(
    val name: String,
    val subtitle: String,
    val description: String,
    val evolution: String,
    val atmosphere: String,
    val themeColor: Color
)

val erygraCities = listOf(
    CityLore(
        name = "السهل الرمادي",
        subtitle = "Ashen Sprawl",
        description = "مدينة أطلال متراصة تغطيها مساحات من الرماد والدخان الخانق. الشوارع تتقاطع كالمتاهة المظلمة، والأنقاض تخفي أسراراً دموية. الخرائط هنا تتغير حين تنهار الجسور الرمادية.",
        evolution = "تتطور مع اللاعب: كلما أضاء البطل الأضواء المنسية، يتراجع الضباب الرمادي لتنكشف منصات علوية ودروب كانت مستحيلة، ولكن ينتبه الحراس لضوئك.",
        atmosphere = "الجو: ضباب أسود خانق. الموسيقى: تشيلو عميق وطقطقة معدنية رتيبة (60 BPM).",
        themeColor = BlightGold
    ),
    CityLore(
        name = "الأرشيف المظلل",
        subtitle = "Veiled Archives",
        description = "متاهة هائلة تحت الأرض من الرفوف العمودية العائمة والوثائق الملعونة. الضوء شحيح، فقط المصابيح الباهتة العائمة تكشف لك هوة لا قعر لها.",
        evolution = "تتطور مع اللاعب: الرفوف تتحرك ميكانيكياً استجابةً للنبضات الروحية للبطل، ما يصنع طرقاً جديدة كلياً ويغلق أخرى، الألغاز تزداد تعقيداً كلما تعمقت.",
        atmosphere = "الجو: غبار الكتب والظلام النيلي. الموسيقى: قيثارة (Harp) دقيقة مع صدى نبضات الساعات.",
        themeColor = RadianceWhite
    ),
    CityLore(
        name = "الأرخبيل المجوف",
        subtitle = "Hollowed Archipelago",
        description = "جزر صخرية متصدعة تسبح في فراغ أسود غريب، مرتبطة ببعضها بحبال بالية وألواح خشبية تصفر فيها الرياح العاتية الممزقة.",
        evolution = "تتطور مع اللاعب: مسار الرياح المظلمة يتغير بناءً على المهام؛ العواصف تفتح مجالات طيران شراعي للبطل، ولكنها تجلب أطياف السماء.",
        atmosphere = "الجو: فضاء معدم ورياح قاصفة. الموسيقى: آلات نفخ خشبية حزينة وصدى صرير الخشب.",
        themeColor = OutlineGray
    ),
    CityLore(
        name = "منحدرات الزجاج",
        subtitle = "Glassfjord Cliffs",
        description = "جروف شاهقة ملساء مبنية من بلورات سوداء وزجاج متشقق. الأسطح زلقة ومميتة، تعكس أوهاماً لمن ينظر إليها مدة طويلة.",
        evolution = "تتطور مع اللاعب: ضربات البطل العنيفة تحطم الزجاج وتصنع شقوقاً يمكن تسلقها، الانعكاسات قد تظهر أعداء غير مرئيين يجب محاربتهم عبر المرآة.",
        atmosphere = "الجو: صقيع وانعكاسات مربكة. الموسيقى: ماريمبا زجاجية ونغمات كريستالية عالية (60 BPM).",
        themeColor = EchoesBlue
    ),
    CityLore(
        name = "آليات الغمر",
        subtitle = "Sunken Clockworks",
        description = "آلة عملاقة غاصت في مياه سوداء لا نهاية لها. تروس برونزية ضخمة تدور ببطء مرعب وسط الشلالات الملوثة والصدأ.",
        evolution = "تتطور مع اللاعب: التفاعل مع صمامات المياه يرفع أو يخفض منسوب البحر الميت، فتغرق مناطق كنت تسير فيها وتبرز أبراج لم تكن تراها.",
        atmosphere = "الجو: رطوبة خانقة وماء أسود مطفي. الموسيقى: إيقاعات تروس معدنية متناغمة وصدى قطرات المياه.",
        themeColor = Color(0xFF7A4E3E)
    ),
    CityLore(
        name = "مستنقعات الجذور السوداء",
        subtitle = "Blackroot Moorlands",
        description = "بحر من السموم العضوية والأشجار الميتة ذات الجذور الحية التي تتلوى في الظلام. الغاز السام هنا يخلق هلوسات من الماضي.",
        evolution = "تتطور مع اللاعب: استخدام ذاكرة البطل يوقظ الجذور لتصنع منصات عبور جديدة، ولكن الاستخدام المفرط يوقظ غضب كائنات المستنقع لتلتف حولك.",
        atmosphere = "الجو: ضباب أخضر سام وظلام بيئي. الموسيقى: طبل قبلي بطيء وأصوات حشرات غير طبيعية.",
        themeColor = VitalityRed
    ),
    CityLore(
        name = "الشق المضيء",
        subtitle = "Luminous Chasm",
        description = "ظلمة حالكة لا يكسرها سوى دقات الكائنات النباتية التي تشع بضوء أزرق وأخضر باهت. مكان سريالي تبدو فيه الذكريات كنجوم طافية.",
        evolution = "تتطور مع اللاعب: نبض الضوء من قناعك يحفز نباتات الكهف للإضاءة بالترتيب متزامن، كاشفة عن غرف خفية أو مهلكة الظلال التي تتهرب من الضوء.",
        atmosphere = "الجو: سكون عميق يتخلله نبض مضيء. الموسيقى: أصوات كورال خافتة جداً، سينثيزر دافئ يبعث على الرهبة (45 BPM).",
        themeColor = Color(0xFFA6F0E6)
    )
)

@Composable
fun ChroniclesScreen(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidPrimary)
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onClose,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RadianceWhite),
                border = BorderStroke(1.dp, RadianceWhite.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(0.dp)
            ) {
                Text("RETURN")
            }
            Text(
                text = "CHRONICLES OF ECHOES",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = EchoesBlue
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .height(2.dp)
                .background(EchoesBlue.copy(alpha = 0.3f))
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            items(erygraCities) { city ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(1.dp, city.themeColor.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = city.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = city.themeColor
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(city.themeColor)
                                    .border(1.dp, VoidPrimary)
                            )
                        }
                        Text(
                            text = city.subtitle,
                            fontSize = 12.sp,
                            color = RadianceWhite.copy(alpha = 0.5f),
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(bottom = 16.dp, end = 24.dp).fillMaxWidth(),
                            textAlign = TextAlign.End
                        )

                        Text(
                            text = city.description,
                            fontSize = 15.sp,
                            color = RadianceWhite,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth(),
                            textAlign = TextAlign.End
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceContainer),
                            shape = RoundedCornerShape(0.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "🌀 التطور الديناميكي:\n${city.evolution}",
                                fontSize = 13.sp,
                                color = EchoesBlue.copy(alpha = 0.8f),
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                textAlign = TextAlign.End
                            )
                        }

                        Text(
                            text = "🎵 ${city.atmosphere}",
                            fontSize = 13.sp,
                            color = BlightGold.copy(alpha = 0.9f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
