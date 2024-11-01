package com.example.parkly

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View

class ParkingLotView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val parkingSpaces = mutableListOf<Rect>()
    private val entrySign = Rect()
    private val exitSign = Rect()
    private val paint = Paint()
    private var offsetX = 0f
    private var offsetY = 0f
    private var scaleFactor = 1f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isDragging = false

    // Layout configuration variables
    private val cols = 10 // Parking spaces per row
    private val spaceWidth = 120 // Width of each parking space
    private val spaceHeight = 200 // Height of each parking space
    private val horizontalAisleSpacing = 40 // Space between columns (gap between parking spaces)
    private val verticalRoadHeight = 80 // Height of the road
    private val verticalAisleSpacing = 30 // Space for roads between groups
    private val startX = 100 // Starting X position
    private val startY = 100 // Starting Y position

    private var maxOffsetX = 0f
    private var maxOffsetY = 0f
    private var currentY = startY // Initialize currentY at the start position

    private val scaleGestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(0.5f, 3f) // Set minimum zoom level to 0.5f
            invalidate()
            return true
        }
    })

    init {
        paint.style = Paint.Style.FILL
        setupParkingLayout()
    }

    private fun setupParkingLayout() {
        // Entry and exit sign setup
        entrySign.set(startX, startY, startX + 100, startY + 50)
        exitSign.set(startX + (cols * (spaceWidth + horizontalAisleSpacing)) - 100, startY, startX + (cols * (spaceWidth + horizontalAisleSpacing)), startY + 50)

        // Start with one row of parking spaces (black)
        for (col in 0 until cols) {
            val left = startX + col * (spaceWidth + horizontalAisleSpacing)
            parkingSpaces.add(Rect(left, currentY, left + spaceWidth, currentY + spaceHeight))
        }
        currentY += spaceHeight // Move down directly for the road below the first row

        // Continue the pattern
        val totalGroups = 3 // Number of groups of two rows of parking spaces
        for (i in 0 until totalGroups) {
            // Road after one row of parking spaces
            parkingSpaces.add(Rect(startX - 20, currentY, startX + cols * (spaceWidth + horizontalAisleSpacing), currentY + verticalRoadHeight))
            currentY += verticalRoadHeight // Move down for the next rows

            // Two rows of parking spaces
            for (j in 0 until 2) {
                for (col in 0 until cols) {
                    val left = startX + col * (spaceWidth + horizontalAisleSpacing)
                    parkingSpaces.add(Rect(left, currentY, left + spaceWidth, currentY + spaceHeight))
                }
                currentY += spaceHeight // Move down for the next row of parking spaces
                if (j == 0) {
                    currentY += verticalAisleSpacing // Add gap after the first row of parking spaces
                }
            }
        }

        // Final road after the last group
        parkingSpaces.add(Rect(startX - 20, currentY, startX + cols * (spaceWidth + horizontalAisleSpacing), currentY + verticalRoadHeight))
        currentY += verticalRoadHeight // Move down for the last row of parking spaces

        // Last row of parking spaces
        for (col in 0 until cols) {
            val left = startX + col * (spaceWidth + horizontalAisleSpacing)
            parkingSpaces.add(Rect(left, currentY, left + spaceWidth, currentY + spaceHeight))

        }

        // Define the position for the left column of parking spaces
        val leftColumnStartX = startX - spaceHeight - 100 // Align closely with the left road
        var leftColumnY = startY + spaceHeight + verticalAisleSpacing // Skip the top space for a blank area

// Add parking spaces from top to bottom until reaching the bottom of the left road
        while (leftColumnY + spaceWidth <= currentY) {
            // Create each parking space with original dimensions (rotated)
            val left = leftColumnStartX
            val top = leftColumnY
            val right = left + spaceHeight // Width is `spaceHeight` to make it horizontal
            val bottom = top + spaceWidth  // Height is `spaceWidth`

            parkingSpaces.add(Rect(left, top, right, bottom))

            // Move down for the next parking space
            leftColumnY += spaceWidth + verticalAisleSpacing
        }

        // Update the positions of the entry and exit signs based on the current layout
        entrySign.set(startX-150, startY-200 , startX + 50, startY -100)
        exitSign.set(startX + (cols * (spaceWidth + horizontalAisleSpacing)) - 100, currentY, startX + (cols * (spaceWidth + horizontalAisleSpacing)), currentY + 50)



        // Calculate the maximum offsets based on the total layout
        maxOffsetX = ((cols * (spaceWidth + horizontalAisleSpacing)) * scaleFactor) - width / scaleFactor
        maxOffsetY = (currentY * scaleFactor) - height / scaleFactor



    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(scaleFactor, scaleFactor)

        // Draw Entry and Exit signs
        paint.color = Color.GREEN
        canvas.drawRect(entrySign, paint)
        paint.textSize = 60f
        paint.color = Color.WHITE
        canvas.drawText("Entry", startX -110f, startY-140f, paint)



        // Draw Parking Spaces
        parkingSpaces.forEachIndexed { index, space ->
            // Change color of the first row of parking spaces to black

                paint.color = Color.GREEN // Other parking spaces

            canvas.drawRect(space, paint)
        }

        // Draw Roads (between groups)
        paint.color = Color.LTGRAY
        for (i in parkingSpaces.indices) {
            val space = parkingSpaces[i]
            if (space.height() == verticalRoadHeight) { // Only draw road rectangles
                canvas.drawRect(space, paint)
            }
        }

        // Draw vertical road on the left side
        paint.color = Color.LTGRAY
        val leftRoad = Rect(
            startX-100,
            startY,
            startX -20,
            currentY
        )
        canvas.drawRect(leftRoad, paint)

        // Draw vertical road on the right side with width 80
        val rightRoad = Rect(
            startX + (cols * (spaceWidth + horizontalAisleSpacing)),
            startY+200,
            startX + (cols * (spaceWidth + horizontalAisleSpacing)) + 80,
            currentY
        )
        canvas.drawRect(rightRoad, paint)

        canvas.restore()
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                isDragging = true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY

                    offsetX = (offsetX + dx).coerceIn(-maxOffsetX, maxOffsetX)
                    offsetY = (offsetY + dy).coerceIn(-maxOffsetY, maxOffsetY)

                    lastTouchX = event.x
                    lastTouchY = event.y

                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
            }
        }
        return true
    }
}







