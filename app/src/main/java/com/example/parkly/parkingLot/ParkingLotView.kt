package com.example.parkly

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.parkly.community.viewmodel.PostViewModel
import com.example.parkly.data.Pdf
import com.example.parkly.data.Reservation
import com.example.parkly.data.viewmodel.UserViewModel
import com.example.parkly.parkingLot.viewmodel.ParkingSpaceViewModel
import com.example.parkly.reservation.viewmodel.ReservationViewModel
import com.example.parkly.util.dialog
import com.example.parkly.util.snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.parkly.util.dialog
import org.joda.time.DateTime


class ParkingLotView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {
    private lateinit var spaceVM: ParkingSpaceViewModel
    private lateinit var reservationVM: ReservationViewModel
    private lateinit var userVM: UserViewModel
    private var lifecycleOwner: LifecycleOwner? = null
    private var Action =""
    private var Date= 0L
    private var StartTime =0
    private var Duration =""
    private var FileUri =""
    private var Reason =""
    private var FileName =""
    // Set ViewModels
    fun setViewModel(viewModel1: ParkingSpaceViewModel, viewModel2: ReservationViewModel,viewModel3:UserViewModel) {
        this.spaceVM = viewModel1
        this.reservationVM = viewModel2
        this.userVM = viewModel3

        // Optionally observe data here, utilizing the lifecycle owner
        lifecycleOwner?.let { owner ->
            spaceVM!!.getParkingSpaceLD().observe(owner) { /* Handle ParkingSpaceViewModel data updates */ }
            reservationVM!!.getreservationLD().observe(owner) { /* Handle ReservationViewModel data updates */ }
            userVM!!.getUserLD().observe(owner) { /* Handle ReservationViewModel data updates */ }
        }
    }

    // Set LifecycleOwner
    fun setLifecycleOwner(owner: LifecycleOwner) {
        lifecycleOwner = owner

        // Rebind existing ViewModel observers when LifecycleOwner is set
        if (::spaceVM.isInitialized && ::reservationVM.isInitialized) {
            spaceVM!!.getParkingSpaceLD().observe(owner) { /* Handle ParkingSpaceViewModel data updates */ }
            reservationVM!!.getreservationLD().observe(owner) { /* Handle ReservationViewModel data updates */ }
            userVM!!.getUserLD().observe(owner) { /* Handle ReservationViewModel data updates */ }
        }
    }

    // Set parameters safely and update internal state
    fun setParameters(action: String, date: Long, startTime: Int, duration: String, fileUri: String, reason: String,fileName: String) {
        // Update only if parameters are not empty or default
        if (action.isNotEmpty() || date != 0L || startTime != 0 || duration.isNotEmpty() || fileUri.isNotEmpty() || reason.isNotEmpty()) {
            Action = action
            Date = date
            StartTime = startTime
            Duration = duration
            FileUri = fileUri
            Reason = reason
            FileName = fileName
        }
    }


    private val nav by lazy { findNavController() }
    private val parkingSpaces = mutableListOf<Rect>()
    private val roadRectangles = mutableListOf<Rect>()
    private val entrySign = Rect()
    private val exitSign = Rect()
    private val buildingSign = Rect()
    private val paint = Paint()
    private var offsetX = 0f
    private var offsetY = 0f
    private var scaleFactor = 1f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isDragging = false
    private val borderPaint = Paint() // Paint for borders
    private val borderRadius = 20f // Radius for rounded corners
    val parkingLotView = findViewById<ParkingLotView>(R.id.parkingLotView)

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
        paint.textAlign = Paint.Align.CENTER

        setupParkingLayout()

        invalidate()


        // Center the map around the Exit sign initially
        val exitCenterX = exitSign.centerX().toFloat()
        val exitCenterY = exitSign.centerY().toFloat()

        // Adjust these additional offsets to control how far to the top-left you want to move the view
        val additionalOffsetX = 900f // Adjust this to move further left (increase value for more left)
        val additionalOffsetY = 1600f // Adjust this to move further up (increase value for more up)

        // Calculate offsets with additional adjustments to position near the top-left corner of the Exit sign
        offsetX = (width / 2 - exitCenterX) + additionalOffsetX
        offsetY = (height / 2 - exitCenterY) + additionalOffsetY

        // Ensure offsets are within the bounds
        offsetX = offsetX.coerceIn(-maxOffsetX, maxOffsetX)
        offsetY = offsetY.coerceIn(-maxOffsetY, maxOffsetY)
        parkingLotView.invalidate()
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
        // Add roads to the list of road rectangles
        parkingSpaces.forEach { space ->
            if (space.width() >200) {
                roadRectangles.add(space)
            }
        }

        // Update the positions of the entry and exit signs based on the current layout
        entrySign.set(startX - 150, startY - 200, startX + 50, startY - 100)
        exitSign.set(3300,2200,3500,2300)
        buildingSign.set(1600,2200,1900,2300)

        // Calculate the maximum offsets based on the total layout
        maxOffsetX = ((cols * (spaceWidth + horizontalAisleSpacing)) * scaleFactor) - width / scaleFactor
        maxOffsetY = (currentY * scaleFactor) - height / scaleFactor

        // Add the same layout on the right side of the right road
        addRightSideParkingLayout()
        invalidate()
    }

    private fun addRightSideParkingLayout() {
        val rightStartX = startX + (cols * (spaceWidth + horizontalAisleSpacing)) + 65 + horizontalAisleSpacing // Position for the right layout
        var rightCurrentY = startY // Reset Y position for the right layout

        // Start adding parking spaces for the right side
        for (col in 0 until cols) {
            val left = rightStartX + col * (spaceWidth + horizontalAisleSpacing)
            parkingSpaces.add(Rect(left, rightCurrentY, left + spaceWidth, rightCurrentY + spaceHeight))
        }
        rightCurrentY += spaceHeight // Move down for the road below the first row

        // Continue the same pattern as before
        for (i in 0 until 3) { // Same number of groups of two rows of parking spaces
            // Road after one row of parking spaces
            parkingSpaces.add(Rect(rightStartX - 20, rightCurrentY, rightStartX + cols * (spaceWidth + horizontalAisleSpacing), rightCurrentY + verticalRoadHeight))
            rightCurrentY += verticalRoadHeight // Move down for the next rows

            // Two rows of parking spaces
            for (j in 0 until 2) {
                for (col in 0 until cols) {
                    val left = rightStartX + col * (spaceWidth + horizontalAisleSpacing)
                    parkingSpaces.add(Rect(left, rightCurrentY, left + spaceWidth, rightCurrentY + spaceHeight))
                }
                rightCurrentY += spaceHeight // Move down for the next row of parking spaces
                if (j == 0) {
                    rightCurrentY += verticalAisleSpacing // Add gap after the first row of parking spaces
                }
            }
        }

        // Final road after the last group on the right side
        parkingSpaces.add(Rect(rightStartX - 20, rightCurrentY, rightStartX + cols * (spaceWidth + horizontalAisleSpacing), rightCurrentY + verticalRoadHeight))
        rightCurrentY += verticalRoadHeight // Move down for the last row of parking spaces

        // Last row of parking spaces on the right
        for (col in 0 until cols) {
            val left = rightStartX + col * (spaceWidth + horizontalAisleSpacing)
            parkingSpaces.add(Rect(left, rightCurrentY, left + spaceWidth, rightCurrentY + spaceHeight))
        }

        // Define the position for the right vertical column of parking spaces
        val rightColumnStartX = rightStartX +(10*160) +80// Adjust to align with the right road
        var rightColumnY = startY + spaceHeight + verticalAisleSpacing // Skip the top space for a blank area

        // Add parking spaces from top to bottom until reaching the bottom of the right road
        while (rightColumnY + spaceWidth <= rightCurrentY) {
            // Create each parking space with original dimensions (rotated)
            val left = rightColumnStartX
            val top = rightColumnY
            val right = left + spaceHeight // Width is `spaceHeight` to make it horizontal
            val bottom = top + spaceWidth  // Height is `spaceWidth`

            parkingSpaces.add(Rect(left, top, right, bottom))

            // Move down for the next parking space
            rightColumnY += spaceWidth + verticalAisleSpacing
        }

        // Update max offset X to accommodate the new layout on the right
        maxOffsetX = ((rightStartX + cols * (spaceWidth + horizontalAisleSpacing)) * scaleFactor) - width / scaleFactor
        invalidate()
    }


    private fun getParkingId(index: Int): String {
        // Prefix "A" for all parking spaces and increment the ID sequentially
        return "A${index + 1}"
    }








    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(scaleFactor, scaleFactor)

        // Draw Entry Sign
        paint.color = Color.RED // Fill color for entry sign
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(entrySign.left.toFloat(), entrySign.top.toFloat(), entrySign.right.toFloat(), entrySign.bottom.toFloat(), borderRadius, borderRadius, paint)

        // Draw red border for Entry Sign
        paint.color = Color.RED // Border color for entry sign
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8f // Border thickness
        canvas.drawRoundRect(entrySign.left.toFloat(), entrySign.top.toFloat(), entrySign.right.toFloat(), entrySign.bottom.toFloat(), borderRadius, borderRadius, paint)

        // Draw text for Entry Sign
        paint.style = Paint.Style.FILL
        paint.color = Color.DKGRAY
        paint.textSize = 60f
        canvas.drawText("Exit", entrySign.centerX().toFloat(), entrySign.centerY().toFloat() + 10, paint) // Center text in the entry sign

        // Draw Exit Sign
        paint.color = Color.GREEN // Fill color for exit sign
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(exitSign.left.toFloat(), exitSign.top.toFloat(), exitSign.right.toFloat(), exitSign.bottom.toFloat(), borderRadius, borderRadius, paint)

        // Draw green border for Exit Sign
        paint.color = Color.GREEN // Border color for exit sign
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(exitSign.left.toFloat(), exitSign.top.toFloat(), exitSign.right.toFloat(), exitSign.bottom.toFloat(), borderRadius, borderRadius, paint)

        // Draw text for Exit Sign
        paint.style = Paint.Style.FILL
        paint.color = Color.DKGRAY
        canvas.drawText("Entry", exitSign.centerX().toFloat(), exitSign.centerY().toFloat() + 10, paint) // Center text in the exit sign

        // Draw Building Sign
        paint.color = Color.YELLOW // Fill color for building sign
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(buildingSign.left.toFloat(), buildingSign.top.toFloat(), buildingSign.right.toFloat(), buildingSign.bottom.toFloat(), borderRadius, borderRadius, paint)

        // Draw yellow border for Building Sign
        paint.color = Color.YELLOW // Border color for building sign
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(buildingSign.left.toFloat(), buildingSign.top.toFloat(), buildingSign.right.toFloat(), buildingSign.bottom.toFloat(), borderRadius, borderRadius, paint)

        // Draw text for Building Sign
        paint.style = Paint.Style.FILL
        paint.color = Color.DKGRAY
        canvas.drawText("Building", buildingSign.centerX().toFloat(), buildingSign.centerY().toFloat() + 10, paint) // Center text in the building sign

        // Draw Parking Spaces and IDs
        paint.textSize = 40f // Increase text size for visibility
        paint.color = Color.DKGRAY // Use darker color for text
        paint.setShadowLayer(4f, 2f, 2f, Color.BLACK) // Add shadow for better contrast
        if (Action == "") {
            // Action is empty, just display parking spaces
            parkingSpaces.forEachIndexed { index, space ->
                // Draw the parking space
                when (spaceVM?.get(getParkingId(index))?.spaceStatus) {
                    "Available" -> paint.color = Color.GREEN
                    "Occupied" -> paint.color = Color.RED
                    "Reserved" -> paint.color = Color.YELLOW
                    else -> paint.color = Color.GRAY // Default color if status is unknown
                }

                canvas.drawRect(space, paint)

                // Draw parking ID in the center
                paint.color = Color.DKGRAY
                val parkingId = getParkingId(index)
                val centerX = space.centerX().toFloat()
                val centerY = space.centerY().toFloat() + 10 // Offset slightly for better centering
                canvas.drawText(parkingId, centerX, centerY, paint)
            }
        } else {
            val reservations = reservationVM?.getAll()
            // Action is not empty, check for reservation conflicts
            parkingSpaces.forEachIndexed { index, space ->


                    // Fetch all reservations and filter by the specific parking space ID
                    val filteredReservations = reservations?.filter { it.spaceID == getParkingId(index) && it.status!="Expired"}
                if (filteredReservations != null) {
                    if(filteredReservations.isNotEmpty()) {
                        // Parse the start and end time parameters
                        val startParam = StartTime
                        val endParam = when (Duration) {
                            "1 Hour" -> startParam + 1
                            "2 Hours" -> startParam + 2
                            "3 Hours" -> startParam + 3
                            else -> startParam + 4
                        }

                        // Check if there is a conflict with any of the filtered reservations
                        val hasConflict = filteredReservations.any { reservation ->
                            reservation.date == Date && // Check if dates match
                                    (reservation.startTime < endParam &&
                                            (reservation.startTime + reservation.duration) > startParam) // Check time overlap
                        }
                        // Safely update the paint color based on conflict detection
                        paint.color = if (hasConflict) Color.RED else Color.GREEN

                    }else{
                        paint.color = Color.GREEN
                    }
                }




                // Draw the parking space rectangle
                canvas.drawRect(space, paint)

                // Draw parking ID in the center
                paint.color = Color.DKGRAY
                val parkingId = getParkingId(index)
                val centerX = space.centerX().toFloat()
                val centerY = space.centerY().toFloat() + 10 // Offset slightly for better centering
                canvas.drawText(parkingId, centerX, centerY, paint)
            }
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
            startX - 100,
            startY,
            startX - 20,
            currentY
        )
        canvas.drawRect(leftRoad, paint)

        // Draw vertical road on the center
        val centerRoad = Rect(
            startX + (cols * (spaceWidth + horizontalAisleSpacing)),
            startY + 200,
            startX + (cols * (spaceWidth + horizontalAisleSpacing)) + 80,
            currentY + 200
        )
        canvas.drawRect(centerRoad, paint)

        // Draw vertical road on the right side
        val rightRoad = Rect(
            startX + ((cols +cols) * (spaceWidth + horizontalAisleSpacing)) + 100,
            startY + 200,
            startX + ((cols +cols)* (spaceWidth + horizontalAisleSpacing)) + 180,
            currentY + 200
        )
        canvas.drawRect(rightRoad, paint)

        // Clear shadow layer after drawing text
        paint.setShadowLayer(0f, 0f, 0f, Color.BLACK)

        canvas.restore()
        invalidate()
    }

    private fun handleParkingSpaceClick(x: Float, y: Float) {
        // Check if the touch coordinates intersect with any road rectangles
        roadRectangles.forEach { road ->
            if (road.contains(((x - offsetX) / scaleFactor).toInt(),
                    ((y - offsetY) / scaleFactor).toInt())) {
                // If the touch is on a road, return without processing parking spaces
                return
            }
        }

        if (Action == "") {
            // Check for each parking space if the touch coordinates intersect
            parkingSpaces.forEachIndexed { index, space ->
                if (space.contains(
                        ((x - offsetX) / scaleFactor).toInt(),
                        ((y - offsetY) / scaleFactor).toInt()
                    )
                ) {
                    // Start a new activity and pass the spaceID

                    if (space.width() <= 200) {
                        val spaceID = getParkingId(index)
                        nav.navigate(
                            R.id.action_parkingLotFragment_to_parkingSpaceDetailsFragment, bundleOf(
                                "spaceID" to spaceID
                            )
                        )
                    }

                }
            }
        }else{
            val reservations = reservationVM?.getAll()
            // Action is not empty, check for reservation conflicts
            parkingSpaces.forEachIndexed { index, space ->

                // Fetch all reservations and filter by the specific parking space ID
                val filteredReservations = reservations?.filter { it.spaceID == getParkingId(index) && it.status!="Expired"}
                if (filteredReservations != null) {
                    if(filteredReservations.isNotEmpty()) {
                        // Parse the start and end time parameters
                        val startParam = StartTime
                        val endParam = when (Duration) {
                            "1 Hour" -> startParam + 1
                            "2 Hours" -> startParam + 2
                            "3 Hours" -> startParam + 3
                            else -> startParam + 4
                        }

                        val finalDuration = when (Duration) {
                            "1 Hour" -> 1
                            "2 Hours" -> 2
                            "3 Hours" -> 3
                            else -> 4
                        }


                        // Check if there is a conflict with any of the filtered reservations
                        val hasConflict = filteredReservations.any { reservation ->
                            reservation.date == Date && // Check if dates match
                                    (reservation.startTime < endParam &&
                                            (reservation.startTime + reservation.duration) > startParam) // Check time overlap
                        }

                        if (!hasConflict) {
                            if (space.contains(
                                    ((x - offsetX) / scaleFactor).toInt(),
                                    ((y - offsetY) / scaleFactor).toInt()
                                )
                            ) {
                                if (space.width() <= 200) {
                                    val spaceID = getParkingId(index)
                                    dialog("Reservation",
                                        "Are you sure you reserve this parking space ?$spaceID",
                                        onPositiveClick = { _, _ ->
                                            lifecycleOwner?.lifecycleScope?.launch {
                                                val fileUriString = FileUri
                                                val fileUri = fileUriString?.let { Uri.parse(it) } // Convert back to Uri if not null

                                                reservationVM?.uploadFile(fileUri!!, FileName )
                                            }

                                            lifecycleOwner?.let {
                                                reservationVM?.supportedFile?.observe(it) {

                                                    if (it.equals(Pdf())) return@observe

                                                    val reservation = Reservation(
                                                        id="",
                                                        userID = userVM.getAuth().uid,
                                                        spaceID=spaceID,
                                                        file = it,
                                                        reason =Reason,
                                                        date = Date,
                                                        startTime =StartTime,
                                                        duration=finalDuration,
                                                        status = "Pending",
                                                        createdAt = DateTime.now().millis
                                                    )

                                                    lifecycleOwner!!.lifecycleScope.launch {
                                                        reservationVM.set(reservation)
                                                        snackbar("Reservation Submitted Successfully.")
                                                        findNavController().navigate(R.id.eventFragment)
                                                    }
                                                }
                                            }
                                        }
                                    )

                                }

                            }
                        }


                    }else{
                        if (space.contains(
                                ((x - offsetX) / scaleFactor).toInt(),
                                ((y - offsetY) / scaleFactor).toInt()
                            )
                        ) {
                            // Start a new activity and pass the spaceID
                            val finalDuration = when (Duration) {
                                "1 Hour" -> 1
                                "2 Hours" -> 2
                                "3 Hours" -> 3
                                else -> 4
                            }

                            if (space.width() <= 200) {
                                val spaceID = getParkingId(index)
                                dialog("Reservation",
                                    "Are you sure you reserve this parking space ?$spaceID",
                                    onPositiveClick = { _, _ ->
                                        lifecycleOwner?.lifecycleScope?.launch {
                                            val fileUriString = FileUri
                                            val fileUri = fileUriString?.let { Uri.parse(it) } // Convert back to Uri if not null

                                            reservationVM?.uploadFile(fileUri!!, FileName )
                                        }

                                        lifecycleOwner?.let {
                                            reservationVM?.supportedFile?.observe(it) {

                                                if (it.equals(Pdf())) return@observe

                                                val reservation = Reservation(
                                                    id="",
                                                    userID = userVM.getAuth().uid,
                                                    spaceID=spaceID,
                                                    file = it,
                                                    reason =Reason,
                                                    date = Date,
                                                    startTime =StartTime,
                                                    duration=finalDuration,
                                                    status = "Pending",
                                                    createdAt = DateTime.now().millis
                                                )

                                                lifecycleOwner!!.lifecycleScope.launch {
                                                    reservationVM.set(reservation)
                                                    snackbar("Reservation Submitted Successfully.")
                                                    findNavController().navigate(R.id.eventFragment)
                                                }
                                            }
                                        }
                                    }
                                )
                            }

                        }
                    }
                }
        }
    }}


    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                isDragging = false // Initialize dragging state as false
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastTouchX
                val dy = event.y - lastTouchY

                // Calculate the distance moved
                val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                // Define a threshold for movement to distinguish between a click and drag
                val dragThreshold = 10f // Adjust this value as needed

                if (distance > dragThreshold) {
                    isDragging = true // Set dragging to true if the distance exceeds the threshold

                    // Update offsets for dragging
                    offsetX = (offsetX + dx).coerceIn(-maxOffsetX, maxOffsetX)
                    offsetY = (offsetY + dy).coerceIn(-maxOffsetY, maxOffsetY)

                    lastTouchX = event.x
                    lastTouchY = event.y

                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (!isDragging) {
                    // Only handle click if it was not a drag
                    handleParkingSpaceClick(event.x, event.y)
                }
                isDragging = false
            }
        }
        return true
    }
}
