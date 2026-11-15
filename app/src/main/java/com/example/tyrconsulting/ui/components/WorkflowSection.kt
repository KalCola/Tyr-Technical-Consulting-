package com.example.tyrconsulting.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tyrconsulting.model.TyrCatalog
import com.example.tyrconsulting.model.WorkflowStep
import com.example.tyrconsulting.ui.theme.*

@Composable
fun WorkflowSection(
    onStepAction: (WorkflowStep) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("workflow_section"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "ᚱ BIFRÖST 3-STEP ONBOARDING WORKFLOW",
            color = AsgardLight,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            letterSpacing = 0.5.sp
        )

        TyrCatalog.workflowSteps.forEach { step ->
            val stepColor = when (step.step) {
                1 -> MidgardMain
                2 -> AsgardMain
                else -> VanaheimMain
            }
            val stepLightColor = when (step.step) {
                1 -> MidgardLight
                2 -> AsgardLight
                else -> VanaheimLight
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("workflow_step_${step.step}"),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(stepColor.copy(alpha = 0.4f))
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "STEP 0${step.step}",
                            color = stepLightColor,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                        Text(
                            text = step.realm,
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = step.title,
                        color = TextMain,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = step.description,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { onStepAction(step) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = stepLightColor
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("workflow_action_${step.step}")
                    ) {
                        Text(step.actionLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
